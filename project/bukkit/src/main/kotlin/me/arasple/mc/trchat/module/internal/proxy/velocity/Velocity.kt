package me.arasple.mc.trchat.module.internal.proxy.velocity

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.internal.proxy.bukkit.Players
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.buildMessage
import me.arasple.mc.trchat.util.proxy.common.MessageReader
import me.arasple.mc.trchat.util.sendProcessedMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.plugin.messaging.PluginMessageRecipient
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit
import java.io.IOException
import java.util.*

/**
 * Velocity
 * me.arasple.mc.trchat.util.proxy.velocity
 *
 * @author wlys
 * @since 2021/8/21 19:15
 */
@Internal
class Velocity : PluginMessageListener {

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != INCOMING_CHANNEL) {
            return
        }
        try {
            val data = MessageReader.read(message)
            if (data.isCompleted) {
                execute(data.build())
            }
        } catch (_: IOException) {
        }
    }

    private fun execute(data: Array<String>) {
        when (data[0]) {
            "PlayerList" -> {
                Players.setPlayers(data[1].split(", "))
            }
            "GlobalMute" -> {
                when (data[1]) {
                    "on" -> TrChat.isGlobalMuting = true
                    "off" -> TrChat.isGlobalMuting = false
                }
            }
            "BroadcastRaw" -> {
                val uuid = UUID.fromString(data[1])
                val raw = data[2]
                val permission = data[3]
                val message = GsonComponentSerializer.gson().deserialize(raw)

                if (permission == "null") {
                    onlinePlayers().forEach { it.sendProcessedMessage(uuid, message) }
                } else {
                    onlinePlayers().filter { it.hasPermission(permission) }.forEach { it.sendProcessedMessage(uuid, message) }
                }
                console().sendProcessedMessage(uuid, message)
            }
        }
    }

    companion object {

        const val INCOMING_CHANNEL = "trchat:server"
        const val OUTGOING_CHANNEL = "trchat:proxy"

        fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChat.plugin, OUTGOING_CHANNEL)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChat.plugin, OUTGOING_CHANNEL)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(TrChat.plugin, INCOMING_CHANNEL)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChat.plugin, INCOMING_CHANNEL, Velocity())
            }
        }

        fun sendBukkitMessage(recipient: PluginMessageRecipient, vararg args: String, async: Boolean = true) {
            submit(async = async) {
                try {
                    for (bytes in buildMessage(*args)) {
                        recipient.sendPluginMessage(TrChat.plugin, OUTGOING_CHANNEL, bytes)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}