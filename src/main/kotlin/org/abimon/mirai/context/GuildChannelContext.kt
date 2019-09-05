package org.abimon.mirai.context

interface GuildChannelContext: ChannelContext {
    val guild: PendingGuildContext
}