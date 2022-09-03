package me.arasple.mc.trchat.module.display

import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.util.classChatSerializer
import me.arasple.mc.trchat.util.color.CustomColor
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.getDataContainer
import me.arasple.mc.trchat.util.gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.flattener.ComponentFlattener
import org.bukkit.entity.Player
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.Packet
import taboolib.module.nms.sendPacket
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author wlys
 * @since 2021/12/11 22:44
 */
class ChatSession(
    val player: Player,
    var channel: String?
) {

    var lastMessage = ""

    var lastPrivateTo = ""

    val receivedMessages = mutableListOf<ChatMessage>()

    fun getColor(default: CustomColor): CustomColor {
        val forces = MessageColors.getForceColors(player)
        return if (forces.isNotEmpty()) {
            CustomColor.get(forces[0])
        } else {
            val selectedColor = player.getDataContainer().getString("color")
            if (selectedColor != null && player.hasPermission(MessageColors.COLOR_PERMISSION_NODE + selectedColor)) {
                CustomColor.get(selectedColor)
            } else {
                default
            }
        }
    }

    fun getChannel(): Channel? {
        channel ?: return null
        return Channel.channels[channel]
    }

    fun addMessage(packet: Packet) {
        receivedMessages += ChatMessage(packet.source, packet.toMessage()?.replace("\\s".toRegex(), "")?.takeLast(48))
        if (receivedMessages.size > 100) {
            receivedMessages.removeFirstOrNull()
        }
    }

    fun removeMessage(message: String) {
        receivedMessages.removeIf { it.message == message }
    }

    fun releaseMessage() {
        val messages = ArrayList(receivedMessages)
        receivedMessages.clear()
        repeat(100) { player.sendMessage("") }
        messages.forEach { player.sendPacket(it.packet) }
    }

    companion object {

        @JvmField
        val SESSIONS = ConcurrentHashMap<UUID, ChatSession>()

        fun getSession(player: Player): ChatSession {
            return SESSIONS.computeIfAbsent(player.uniqueId) {
                ChatSession(player, Settings.defaultChannel)
            }
        }

        fun removeSession(player: Player) {
            SESSIONS.remove(player.uniqueId)
        }

        private fun Packet.toMessage(): String? {
            return kotlin.runCatching {
                val component = if (MinecraftVersion.majorLegacy >= 11900) {
                    val json = classChatSerializer.invokeMethod<String>("a", source.invokeMethod<Any>("content")!!, isStatic = true)!!
                    gson(json)
                } else if (!TrChatBukkit.paperEnv) {
                    val json = classChatSerializer.invokeMethod<String>("a", read<Any>("a")!!, isStatic = true)!!
                    gson(json)
                } else {
                    read<Component>("adventure\$message")!!
                }
                var string = ""
                ComponentFlattener.textOnly().flatten(component) { string += it }
                string
            }.getOrNull()
        }

        data class ChatMessage(val packet: Any, val message: String?)
    }
}