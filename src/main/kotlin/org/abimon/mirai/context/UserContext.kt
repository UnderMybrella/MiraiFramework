package org.abimon.mirai.context

import discord4j.core.`object`.util.Snowflake

interface UserContext {
    val id: Snowflake
    val username: String
    val discriminator: String
    val avatarUrl: String?
    val isBot: Boolean
}