package org.abimon.mirai.context

import discord4j.core.`object`.data.stored.MessageBean
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import java.time.Instant
import java.time.format.DateTimeFormatter

data class BaseMessageContext(
    override val channel: PendingChannelContext,
    override val guild: PendingGuildContext?,
    override val author: PendingPossibleMemberContext?,
    override val id: Snowflake,
    override val content: String?,
    override val timestamp: Instant,
    override val editedTimestamp: Instant?,
    override val isTextToSpeech: Boolean,
    override val mentionsEveryone: Boolean,
    override val userMentions: Array<PendingUserContext>,
    override val roleMentions: Array<PendingRoleContext>
) : MessageContext {
    companion object {
        @Suppress("UNNECESSARY_SAFE_CALL")
        operator fun invoke(msg: MessageBean): MessageContext {
            val pendingChannelContext = PendingChannelContext(Snowflake.of(msg.channelId))
            val pendingGuildContext = PendingGuildContext(pendingChannelContext)
            val pendingUserContext = msg.author?.let { user -> PendingUserContext(BaseUserContext(user)) }
            val pendingPossibleMemberContext =
                pendingUserContext?.let { user -> PendingPossibleMemberContext(pendingGuildContext, user) }

            return BaseMessageContext(
                pendingChannelContext,
                pendingGuildContext,
                pendingPossibleMemberContext,
                Snowflake.of(msg.id),
                msg.content?.takeIf(String::isNotEmpty),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(msg.timestamp, Instant::from),
                if (msg.editedTimestamp == null) null else DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(
                    msg.editedTimestamp,
                    Instant::from
                ),
                msg.isTts,
                msg.isMentionEveryone,
                msg.mentions.map { id -> PendingUserContext(Snowflake.of(id)) }.toTypedArray(),
                msg.mentionRoles.map { id -> PendingRoleContext(Snowflake.of(id)) }.toTypedArray()
            )
        }
    }

    constructor(msg: Message) : this(
        PendingChannelContext(msg.channelId),
        PendingGuildContext(msg.guild),
        PendingPossibleMemberContext(msg.authorAsMember, msg.author.orElse(null)),
        msg.id,
        msg.content.orElse(null),
        msg.timestamp,
        msg.editedTimestamp.orElse(null),
        msg.isTts,
        msg.mentionsEveryone(),
        msg.userMentionIds.map(PendingUserContext.Companion::invoke).toTypedArray(),
        msg.roleMentionIds.map(PendingRoleContext.Companion::invoke).toTypedArray()
    )
    
    constructor(event: MessageCreateEvent): this(
        PendingChannelContext(event.message.channelId),
        event.guildId.map(PendingGuildContext.Companion::invoke).orElse(null),
        PendingPossibleMemberContext(event.message.authorAsMember, event.message.author.orElse(null)),
        event.message.id,
        event.message.content.orElse(null),
        event.message.timestamp,
        event.message.editedTimestamp.orElse(null),
        event.message.isTts,
        event.message.mentionsEveryone(),
        event.message.userMentionIds.map(PendingUserContext.Companion::invoke).toTypedArray(),
        event.message.roleMentionIds.map(PendingRoleContext.Companion::invoke).toTypedArray()
    )
}