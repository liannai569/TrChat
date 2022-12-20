package me.arasple.mc.trchat.module.display.channel.obj

import me.arasple.mc.trchat.module.internal.script.Condition
import taboolib.library.configuration.Conversion
import taboolib.library.configuration.Converter
import taboolib.library.configuration.Path

/**
 * @author wlys
 * @since 2022/2/5 13:25
 */
class ChannelSettings(
    @Path("Join-Permission") val joinPermission: String = "",
    @Path("Speak-Condition") val speakCondition: Condition = Condition.EMPTY,
    @Path("Auto-Join") val autoJoin: Boolean = true,
    @Path("Private") val isPrivate: Boolean = false,
    @Path("Target") @Conversion(Range.TargetConverter::class) val range: Range = Range(Range.Type.ALL, -1),
    @Path("Proxy") val proxy: Boolean = false,
    @Path("Double-Transfer") val doubleTransfer: Boolean = true,
    @Path("Ports") @Conversion(PortConverter::class) val ports: List<Int> = emptyList(),
    @Path("Disabled-Functions") val disabledFunctions: List<String> = emptyList(),
    @Path("Filter-Before-Sending") val filterBeforeSending: Boolean = false
) {

    class PortConverter : Converter<List<Int>, String> {
        override fun convertToField(value: String): List<Int> {
            return value.split(";").map { it.toInt() }
        }

        override fun convertFromField(value: List<Int>): String {
            return value.joinToString(";")
        }
    }

}