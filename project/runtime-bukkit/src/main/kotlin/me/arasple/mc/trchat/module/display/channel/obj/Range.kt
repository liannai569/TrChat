package me.arasple.mc.trchat.module.display.channel.obj

import taboolib.library.configuration.Converter

/**
 * @author ItsFlicker
 * @since 2022/2/6 11:13
 */
class Range(val type: Type, val distance: Int) {

    enum class Type {

        ALL, SINGLE_WORLD, DISTANCE, SELF
    }

    class TargetConverter : Converter<Range, String> {
        override fun convertToField(value: String): Range {
            val args = value.uppercase().split(";", limit = 2)
            val distance = args.getOrNull(1)?.toInt() ?: -1
            return Range(Type.valueOf(args[0]), distance)
        }

        override fun convertFromField(value: Range): String {
            return "${value.type};${value.distance}"
        }
    }

}