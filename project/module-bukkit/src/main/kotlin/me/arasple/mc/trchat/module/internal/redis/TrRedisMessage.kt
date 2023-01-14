package me.arasple.mc.trchat.module.internal.redis

import me.arasple.mc.trchat.util.gson
import net.kyori.adventure.text.Component
import taboolib.library.configuration.Conversion
import taboolib.library.configuration.Converter
import taboolib.module.configuration.UUIDConverter
import java.util.*

class TrRedisMessage(
    @Conversion(ComponentConverter::class) val component: Component = Component.empty(),
    @Conversion(UUIDConverter::class) val sender: UUID? = null,
    val target: String? = null,
    val permission: String? = null
) {

    private class ComponentConverter : Converter<Component, String> {
        override fun convertToField(value: String): Component {
            return gson(value)
        }

        override fun convertFromField(value: Component): String {
            return gson(value)
        }
    }

}