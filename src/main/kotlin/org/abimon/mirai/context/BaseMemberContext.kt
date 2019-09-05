package org.abimon.mirai.context

import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.util.Snowflake
import java.time.Instant

data class BaseMemberContext(
    override val nickname: String,
    override val roles: Array<PendingRoleContext>,
    override val joinedAt: Instant,
    override val id: Snowflake,
    override val username: String,
    override val discriminator: String,
    override val avatarUrl: String?,
    override val isBot: Boolean
) : MemberContext {
    constructor(member: Member): this(
        member.nickname.orElse(null),
        member.roleIds.map(PendingRoleContext.Companion::invoke).toTypedArray(),
        member.joinTime,
        member.id,
        member.username,
        member.discriminator,
        member.avatarUrl,
        member.isBot
    )
}