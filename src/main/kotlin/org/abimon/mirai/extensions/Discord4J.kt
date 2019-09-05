package org.abimon.mirai.extensions

import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.reactive.awaitFirstOrNull

suspend fun DiscordClient.getChannelByIdAwait(channelId: Snowflake): Channel? =
    getChannelById(channelId).awaitFirstOrNull()

suspend fun DiscordClient.getGuildByIdAwait(guildId: Snowflake): Guild? =
    getGuildById(guildId).awaitFirstOrNull()

suspend fun DiscordClient.getGuildEmojiByIdAwait(guildId: Snowflake, emojiId: Snowflake): GuildEmoji? =
    getGuildEmojiById(guildId, emojiId).awaitFirstOrNull()

suspend fun DiscordClient.getMemberByIdAwait(guildId: Snowflake, userId: Snowflake): Member? =
    getMemberById(guildId, userId).awaitFirstOrNull()

suspend fun DiscordClient.getMessageByIdAwait(channelId: Snowflake, messageId: Snowflake): Message? =
    getMessageById(channelId, messageId).awaitFirstOrNull()

suspend fun DiscordClient.getRoleByIdAwait(guildId: Snowflake, roleId: Snowflake): Role? =
    getRoleById(guildId, roleId).awaitFirstOrNull()

suspend fun DiscordClient.getUserByIdAwait(userId: Snowflake): User? =
    getUserById(userId).awaitFirstOrNull()