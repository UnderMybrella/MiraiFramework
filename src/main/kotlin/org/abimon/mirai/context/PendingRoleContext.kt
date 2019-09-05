package org.abimon.mirai.context

import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.util.Snowflake
import reactor.core.publisher.Mono
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class PendingRoleContext {
    companion object {
        operator fun invoke(id: Snowflake) = Missing(id)
        operator fun invoke(context: RoleContext) = Present(context)
        operator fun invoke(mono: Mono<Role>) = Pending(mono)
    }
    data class Present(val roleContext: RoleContext): PendingRoleContext(), RoleContext by roleContext
    data class Missing(val id: Snowflake): PendingRoleContext() {

    }
    data class Pending(val mono: Mono<Role>): PendingRoleContext() {
        suspend fun fullfil(): RoleContext? = null //mono.awaitFirstOrNull()?.let(::BaseRoleContext)
    }
}

@ExperimentalContracts
fun PendingRoleContext.isPresent(): Boolean {
    contract {
        returns() implies (this@isPresent is PendingRoleContext.Present)
    }

    return this is PendingRoleContext.Present
}