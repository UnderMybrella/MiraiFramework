package org.abimon.mirai.context

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.abimon.mirai.Mirai
import org.abimon.mirai.extensions.getGuildByIdAwait
import reactor.core.publisher.Mono
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class PendingGuildContext {
    companion object {
        operator fun invoke(id: Snowflake) = Missing(id)
        operator fun invoke(context: GuildContext) = Present(context)
        operator fun invoke(mono: Mono<Guild>) = Pending(mono)
        operator fun invoke(channel: PendingChannelContext) = PendingChannel(channel)
    }

    data class Present(val guildContext: GuildContext) : PendingGuildContext(), GuildContext by guildContext {
        override suspend fun fulfill(mirai: Mirai): GuildContext? = this
        override suspend fun fulfillId(mirai: Mirai): Snowflake? = guildContext.id
    }

    data class Missing(val id: Snowflake) : PendingGuildContext() {
        private val _value =
            CachedSuspendValue<GuildContext> { mirai -> mirai.client.getGuildByIdAwait(id)?.let(::BaseGuildContext) }

        override suspend fun fulfill(mirai: Mirai): GuildContext? = _value.fulfill(mirai)
        override suspend fun fulfillId(mirai: Mirai): Snowflake? = id
    }

    data class Pending(val mono: Mono<Guild>) : PendingGuildContext() {
        private val _value = CachedSuspendValue<GuildContext> { mono.awaitFirstOrNull()?.let(::BaseGuildContext) }

        override suspend fun fulfill(mirai: Mirai): GuildContext? = _value.fulfill(mirai)
        override suspend fun fulfillId(mirai: Mirai): Snowflake? = fulfill(mirai)?.id
    }

    data class PendingChannel(val channelContext: PendingChannelContext) : PendingGuildContext() {
        private val _value = CachedSuspendValue { mirai ->
            when (val channel = channelContext.fulfill(mirai)) {
                is GuildChannelContext -> channel.guild.fulfill(mirai)
                else -> null
            }
        }

        override suspend fun fulfill(mirai: Mirai): GuildContext? = _value.fulfill(mirai)
        override suspend fun fulfillId(mirai: Mirai): Snowflake? = fulfill(mirai)?.id
    }

    abstract suspend fun fulfillId(mirai: Mirai): Snowflake?
    abstract suspend fun fulfill(mirai: Mirai): GuildContext?
}

@ExperimentalContracts
fun PendingGuildContext.isPresent(): Boolean {
    contract {
        returns() implies (this@isPresent is PendingGuildContext.Present)
    }

    return this is PendingGuildContext.Present
}