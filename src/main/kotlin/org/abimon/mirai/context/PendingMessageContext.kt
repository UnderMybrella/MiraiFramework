package org.abimon.mirai.context

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.reactive.awaitFirstOrNull
import reactor.core.publisher.Mono
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class PendingMessageContext {
    companion object {
        operator fun invoke(id: Snowflake) = Missing(id)
        operator fun invoke(context: MessageContext) = Present(context)
        operator fun invoke(mono: Mono<Message>) = Pending(mono)
    }
    data class Present(val messageContext: MessageContext): PendingMessageContext(), MessageContext by messageContext
    data class Missing(val id: Snowflake): PendingMessageContext() {

    }
    data class Pending(val mono: Mono<Message>): PendingMessageContext() {
        suspend fun fullfil(): MessageContext? = mono.awaitFirstOrNull()?.let(::BaseMessageContext)
    }
}

@ExperimentalContracts
fun PendingMessageContext.isPresent(): Boolean {
    contract {
        returns() implies (this@isPresent is PendingMessageContext.Present)
    }

    return this is PendingMessageContext.Present
}