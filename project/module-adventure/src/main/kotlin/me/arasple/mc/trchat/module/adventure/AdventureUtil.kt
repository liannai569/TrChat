package me.arasple.mc.trchat.module.adventure

import me.arasple.mc.trchat.api.nms.NMS
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.nms.Packet

private val legacySerializer: Any? = try {
    LegacyComponentSerializer.legacySection()
} catch (_: Throwable) {
    null
}

private val gsonSerializer: Any? = try {
    GsonComponentSerializer.gson()
} catch (_: Throwable) {
    null
}

fun gson(component: Component) = (gsonSerializer as GsonComponentSerializer).serialize(component)

fun gson(string: String) = (gsonSerializer as GsonComponentSerializer).deserialize(string)

fun Packet.getComponent(): ComponentText? {
    return when (name) {
        "ClientboundSystemChatPacket" -> {
//            val raw = if (gsonSerializer != null) {
//                gson(source.invokeMethod<Component>("adventure\$content")!!)
//            } else {
//                NMS.instance.rawMessageFromCraftChatMessage(source.invokeMethod<Any>("content")!!)
//            }
            val raw = source.invokeMethod<String>("content", findToParent = false, remap = false) ?: return null
            Components.parseRaw(raw)
        }
        "PacketPlayOutChat" -> {
//            val raw = if (gsonSerializer != null) {
//                gson(read<Component>("adventure\$message")!!)
//            } else {
//                NMS.instance.rawMessageFromCraftChatMessage(read<Any>("a")!!)
//            }
            val raw = NMS.instance.rawMessageFromCraftChatMessage(read<Any>("a") ?: return null)
            Components.parseRaw(raw)
        }
        else -> error("Unsupported packet $name")
    }
}