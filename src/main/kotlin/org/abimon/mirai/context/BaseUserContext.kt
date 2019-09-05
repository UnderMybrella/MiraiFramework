package org.abimon.mirai.context

import discord4j.core.`object`.data.stored.UserBean
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Image
import discord4j.core.`object`.util.Snowflake
import java.time.Instant

data class BaseUserContext(
    override val id: Snowflake,
    override val username: String,
    override val discriminator: String,
    override val avatarUrl: String?,
    override val isBot: Boolean
) : PossibleMemberContext {
    override val joinedAt: Instant? = null
    override val nickname: String? = null
    override val roles: Array<PendingRoleContext>? = null

    constructor(bean: UserBean) : this(
        Snowflake.of(bean.id),
        bean.username,
        bean.discriminator,
        bean.avatar,
        bean.isBot
    )

    constructor(user: User) : this(
        user.id,
        user.username,
        user.discriminator,
        user.getAvatarUrl(if (user.hasAnimatedAvatar()) Image.Format.GIF else Image.Format.PNG).orElse(null),
        user.isBot
    )
}