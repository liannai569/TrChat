package me.arasple.mc.trchat.module.internal.proxy.bungee

import com.google.common.io.ByteStreams
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
 * @author Arasple
 * @date 2019/8/4 21:23
 */
@Internal
class Bungees : PluginMessageListener {

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel == BUNGEE_CHANNEL) {
            try {
                val data = ByteStreams.newDataInput(message)
                val subChannel = data.readUTF()
                if (subChannel == "PlayerList") {
                    data.readUTF() // server
                    Players.setPlayers(data.readUTF().split(", "))
                }
            } catch (_: IOException) {
            }
        }
        if (channel == TRCHAT_CHANNEL) {
            try {
                val data = MessageReader.read(message)
                if (data.isCompleted) {
                    execute(data.build())
                }
            } catch (_: IOException) {
            }
        }
    }

    private fun execute(data: Array<String>) {
        when (data[0]) {
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

        const val TRCHAT_CHANNEL = "trchat:main"
        const val BUNGEE_CHANNEL = "BungeeCord"

        fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChat.plugin, BUNGEE_CHANNEL)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChat.plugin, BUNGEE_CHANNEL)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(TrChat.plugin, BUNGEE_CHANNEL)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChat.plugin, BUNGEE_CHANNEL, Bungees())
            }
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChat.plugin, TRCHAT_CHANNEL)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChat.plugin, TRCHAT_CHANNEL)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(TrChat.plugin, TRCHAT_CHANNEL)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChat.plugin, TRCHAT_CHANNEL, Bungees())
            }
        }

        fun sendBukkitMessage(recipient: PluginMessageRecipient, vararg args: String, async: Boolean = true) {
            submit(async = async) {
                try {
                    for (bytes in buildMessage(*args)) {
                        recipient.sendPluginMessage(TrChat.plugin, TRCHAT_CHANNEL, bytes)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun sendBungeeData(recipient: PluginMessageRecipient, vararg args: String) {
            val out = ByteStreams.newDataOutput()

            for (arg in args) {
                try {
                    out.writeUTF(arg)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            recipient.sendPluginMessage(TrChat.plugin, BUNGEE_CHANNEL, out.toByteArray())
        }
    }
}