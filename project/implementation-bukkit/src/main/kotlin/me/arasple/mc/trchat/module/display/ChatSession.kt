package me.arasple.mc.trchat.module.display

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.util.getDataContainer
import me.arasple.mc.trchat.util.gson
import net.kyori.adventure.text.flattener.ComponentFlattener
import org.bukkit.entity.Player
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.Packet
import taboolib.module.nms.sendPacket
import java.util.*

/**
 * @author wlys
 * @since 2021/12/11 22:44
 */
class ChatSession(
    val player: Player,
    var channel: Channel?,
    var recipients: Set<Player>
) {

    var lastMessage = ""

    var lastPrivateTo = ""

    val receivedMessages = mutableListOf<ChatMessage>()

    val isSpying get() = player.getDataContainer().getBoolean("spying", false)

    val isFilterEnabled get() = player.getDataContainer().getBoolean("filter", true)

    val muteTime get() = player.getDataContainer().getLong("mute_time", 0)

    val isMuted get() = muteTime > System.currentTimeMillis()

    val muteReason get() = player.getDataContainer().getString("mute_reason", "null")!!

    val isVanishing get() = player.getDataContainer().getBoolean("vanish", false)

    fun setFilter(value: Boolean) {
        player.getDataContainer()["filter"] = value
    }

    fun updateMuteTime(time: Long) {
        player.getDataContainer()["mute_time"] = System.currentTimeMillis() + time
    }

    fun setMuteReason(reason: String?) {
        player.getDataContainer()["mute_reason"] = reason
    }

    fun switchSpy(): Boolean {
        player.getDataContainer()["spying"] = !isSpying
        return isSpying
    }

    fun switchVanish(): Boolean {
        player.getDataContainer()["vanish"] = !isVanishing
        return isVanishing.also {
            if (it) vanishing.add(player.name) else vanishing.remove(player.name)
        }
    }

    internal fun addMessage(packet: Packet) {
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
        val SESSIONS = mutableMapOf<UUID, ChatSession>()

        val vanishing = mutableSetOf<String>()

        fun getSession(player: Player): ChatSession {
            return SESSIONS.computeIfAbsent(player.uniqueId) {
                ChatSession(player, Channel.defaultChannel, onlinePlayers().map { it.cast<Player>() }.toSet()).also {
                    if (it.isVanishing) vanishing.add(player.name)
                }
            }
        }

        fun removeSession(player: Player) {
            SESSIONS.remove(player.uniqueId)
        }

        private fun Packet.toMessage(): String? {
            return kotlin.runCatching {
                val iChat = if (MinecraftVersion.majorLegacy >= 11700) {
                    read<Any>("message")!!
                } else {
                    read<Any>("a")!!
                }
                val json = TrChatAPI.classChatSerializer.invokeMethod<String>("a", iChat, fixed = true)!!
                val component = gson(json)
                var string = ""
                ComponentFlattener.textOnly().flatten(component) { string += it }
                string
            }.getOrNull()
        }

        data class ChatMessage(val packet: Any, val message: String?)
    }
}