package me.arasple.mc.trchat.module.display.channel.obj

import me.arasple.mc.trchat.module.internal.script.Condition

/**
 * @author wlys
 * @since 2022/2/5 13:25
 */
class ChannelSettings(
    val joinPermission: String?,
    val speakCondition: Condition?,
    val target: Target,
    val autoJoin: Boolean,
    val proxy: Boolean,
    val doubleTransfer: Boolean,
    val ports: List<Int>?,
    val disabledFunctions: List<String>,
    val filterBeforeSending: Boolean
)