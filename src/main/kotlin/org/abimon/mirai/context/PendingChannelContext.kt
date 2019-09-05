package org.abimon.mirai.context

import discord4j.core.`object`.entity.Channel
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.abimon.mirai.Mirai
import org.abimon.mirai.extensions.getChannelByIdAwait
import reactor.core.publisher.Mono
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class PendingChannelContext {
    companion object {
        operator fun invoke(id: Snowflake) = Missing(id)
        operator fun invoke(context: ChannelContext) = Present(context)
        operator fun invoke(mono: Mono<Channel>) = Pending(mono)
    }

    data class Present(val channelContext: ChannelContext) : PendingChannelContext(), ChannelContext by channelContext {
        override suspend fun fulfill(mirai: Mirai): ChannelContext = this
        override suspend fun fulfillId(mirai: Mirai): Snowflake = this.id
    }

    data class Missing(val id: Snowflake) : PendingChannelContext() {
        private val _value =
            CachedSuspendValue { mirai -> mirai.client.getChannelByIdAwait(id)?.let(::channelContextFor) }

        override suspend fun fulfill(mirai: Mirai): ChannelContext? = _value.fulfill(mirai)
        override suspend fun fulfillId(mirai: Mirai): Snowflake = id
    }

    data class Pending(val mono: Mono<Channel>) : PendingChannelContext() {
        private val _value = CachedSuspendValue { mono.awaitFirstOrNull()?.let(::channelContextFor) }

        override suspend fun fulfill(mirai: Mirai): ChannelContext? = _value.fulfill(mirai)
        override suspend fun fulfillId(mirai: Mirai): Snowflake? = fulfill(mirai)?.id
    }

    abstract suspend fun fulfill(mirai: Mirai): ChannelContext?
    abstract suspend fun fulfillId(mirai: Mirai): Snowflake?
}

@ExperimentalContracts
fun PendingChannelContext.isPresent(): Boolean {
    contract {
        returns() implies (this@isPresent is PendingChannelContext.Present)
    }

    return this is PendingChannelContext.Present
}