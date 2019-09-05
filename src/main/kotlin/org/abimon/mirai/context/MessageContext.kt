package org.abimon.mirai.context

import discord4j.core.`object`.util.Snowflake
import java.time.Instant

interface MessageContext {
    val channel: PendingChannelContext
    val guild: PendingGuildContext?
    val author: PendingPossibleMemberContext?

    val id: Snowflake
    val content: String?
    val timestamp: Instant
    val editedTimestamp: Instant?
    val isTextToSpeech: Boolean
    val mentionsEveryone: Boolean

    val userMentions: Array<PendingUserContext>
    val roleMentions: Array<PendingRoleContext>
}