package org.abimon.mirai.context

import discord4j.core.`object`.util.PermissionSet
import discord4j.core.`object`.util.Snowflake
import java.awt.Color

interface RoleContext {
    val id: Snowflake
    val name: String

    val position: Int
    val colour: Color
    val permissions: PermissionSet

    val isHoisted: Boolean
    val isManaged: Boolean
    val isMentionable: Boolean
}