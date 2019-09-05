package org.abimon.mirai.context

import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.abimon.mirai.Mirai
import org.abimon.mirai.extensions.getMemberByIdAwait
import org.abimon.mirai.extensions.getUserByIdAwait
import reactor.core.publisher.Mono
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class PendingPossibleMemberContext {
    companion object {
        operator fun invoke(guildId: Snowflake, userId: Snowflake) = Missing(guildId, userId)
        operator fun invoke(context: PossibleMemberContext) = Present(context)
        operator fun invoke(mono: Mono<Member>, user: User?) = Pending(mono, user)
        operator fun invoke(guild: PendingGuildContext, user: PendingUserContext) = PendingGuild(guild, user)
    }

    data class Present(val possibleMemberContext: PossibleMemberContext) : PendingPossibleMemberContext(),
        PossibleMemberContext by possibleMemberContext {
        override suspend fun fulfill(mirai: Mirai): PossibleMemberContext = this
    }

    data class Missing(val guildId: Snowflake, val userId: Snowflake) : PendingPossibleMemberContext() {
        private val _value = CachedSuspendValue<PossibleMemberContext> { mirai ->
            mirai.client.getMemberByIdAwait(guildId, userId)?.let(::BaseMemberContext)
                ?: mirai.client.getUserByIdAwait(userId)?.let(::BaseUserContext)
        }

        override suspend fun fulfill(mirai: Mirai): PossibleMemberContext? = _value.fulfill(mirai)
    }

    data class Pending(val mono: Mono<Member>, val fallbackUser: User?) : PendingPossibleMemberContext() {
        override suspend fun fulfill(mirai: Mirai): PossibleMemberContext? =
            mono.awaitFirstOrNull()?.let(::BaseMemberContext)
                ?: fallbackUser?.let(::BaseUserContext)
    }

    data class PendingGuild(val guildContext: PendingGuildContext, val userContext: PendingUserContext) :
        PendingPossibleMemberContext() {
        val _value = CachedSuspendValue<PossibleMemberContext> { mirai ->
            val userId = userContext.fulfillId(mirai)
            if (userId == null) {
                null
            } else {
                val guildId = guildContext.fulfillId(mirai)

                if (guildId == null) {
                    userContext.fulfill(mirai)?.let(::DelegatedUserContext)
                } else {
                    mirai.client.getMemberByIdAwait(guildId, userId)?.let { member -> BaseMemberContext(member) as PossibleMemberContext }
                        ?: userContext.fulfill(mirai)?.let(::DelegatedUserContext)
                }
            }
        }

        override suspend fun fulfill(mirai: Mirai): PossibleMemberContext? = _value.fulfill(mirai)
    }

    abstract suspend fun fulfill(mirai: Mirai): PossibleMemberContext?
}

@ExperimentalContracts
fun PendingPossibleMemberContext.isPresent(): Boolean {
    contract {
        returns() implies (this@isPresent is PendingPossibleMemberContext.Present)
    }

    return this is PendingPossibleMemberContext.Present
}