package org.abimon.mirai.context

import discord4j.core.`object`.util.Snowflake

interface GuildContext {
    val id: Snowflake
}