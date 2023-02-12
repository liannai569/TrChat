package me.arasple.mc.trchat.module.internal.redis

import taboolib.library.configuration.Conversion
import taboolib.library.configuration.Converter
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.configuration.UUIDConverter
import java.util.*

class TrRedisMessage(
    @Conversion(ComponentConverter::class) val component: ComponentText = Components.empty(),
    @Conversion(UUIDConverter::class) val sender: UUID? = null,
    val target: String? = null,
    val permission: String? = null
) {

    private class ComponentConverter : Converter<ComponentText, String> {
        override fun convertToField(value: String): ComponentText {
            return Components.parseRaw(value)
        }

        override fun convertFromField(value: ComponentText): String {
            return value.toRawMessage()
        }
    }

}