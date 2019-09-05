package org.abimon.mirai.context

import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.abimon.mirai.Mirai
import org.abimon.mirai.extensions.getUserByIdAwait
import reactor.core.publisher.Mono
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class PendingUserContext {
    companion object {
        operator fun invoke(id: Snowflake) = Missing(id)
        operator fun invoke(context: UserContext) = Present(context)
        operator fun invoke(mono: Mono<User>) = Pending(mono)
    }
    data class Present(val userContext: UserContext): PendingUserContext(), UserContext by userContext {
        override suspend fun fulfill(mirai: Mirai): UserContext = this
        override suspend fun fulfillId(mirai: Mirai): Snowflake = this.id
    }
    data class Missing(val id: Snowflake): PendingUserContext() {
        private val mutex: Mutex = Mutex()
        private var _value: UserContext? = null
        private var _retry = true

        override suspend fun fulfill(mirai: Mirai): UserContext? = mutex.withLock {
            if (_retry) {
                _value = mirai.client.getUserByIdAwait(id)?.let(::BaseUserContext)
                _retry = false
            }

            _value
        }
        override suspend fun fulfillId(mirai: Mirai): Snowflake? = id
    }
    data class Pending(val mono: Mono<User>): PendingUserContext() {
        private val mutex: Mutex = Mutex()
        private var _value: UserContext? = null
        private var _retry = true

        override suspend fun fulfill(mirai: Mirai): UserContext? = mutex.withLock {
            if (_retry) {
                _value = mono.awaitFirstOrNull()?.let(::BaseUserContext)
                _retry = false
            }

            _value
        }
        override suspend fun fulfillId(mirai: Mirai): Snowflake? = fulfill(mirai)?.id
    }

    abstract suspend fun fulfillId(mirai: Mirai): Snowflake?
    abstract suspend fun fulfill(mirai: Mirai): UserContext?
}

@ExperimentalContracts
fun PendingUserContext.isPresent(): Boolean {
    contract {
        returns() implies (this@isPresent is PendingUserContext.Present)
    }

    return this is PendingUserContext.Present
}