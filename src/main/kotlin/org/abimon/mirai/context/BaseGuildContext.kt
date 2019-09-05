package org.abimon.mirai.context

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.util.Snowflake

data class BaseGuildContext(override val id: Snowflake) : GuildContext {
    constructor(guild: Guild): this(guild.id)
}