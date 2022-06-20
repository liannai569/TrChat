package me.arasple.mc.trchat.module.internal.proxy

import com.google.common.io.ByteStreams
import me.arasple.mc.trchat.module.conf.Loader
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.util.buildMessage
import me.arasple.mc.trchat.util.print
import me.arasple.mc.trchat.util.proxy.common.MessageReader
import me.arasple.mc.trchat.util.sendChatComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.plugin.messaging.PluginMessageRecipient
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit
import java.io.IOException
import java.util.*

/**
 * @author wlys
 * @since 2022/6/18 16:17
 */
interface BukkitProxyProcessor : PluginMessageListener {

    fun init()

    fun sendCommonMessage(recipient: PluginMessageRecipient, vararg args: String, async: Boolean = true): Boolean

    fun sendTrChatMessage(recipient: PluginMessageRecipient, vararg args: String, async: Boolean = true): Boolean

    fun execute(data: Array<String>) {
        when (data[0]) {
            "GlobalMute" -> {
                when (data[1]) {
                    "on" -> TrChatBukkit.isGlobalMuting = true
                    "off" -> TrChatBukkit.isGlobalMuting = false
                }
            }
            "BroadcastRaw" -> {
                val uuid = UUID.fromString(data[1])
                val raw = data[2]
                val permission = data[3]
                val message = GsonComponentSerializer.gson().deserialize(raw)

                if (permission == "null") {
                    onlinePlayers().forEach { it.sendChatComponent(uuid, message) }
                } else {
                    onlinePlayers().filter { it.hasPermission(permission) }.forEach { it.sendChatComponent(uuid, message) }
                }
                console().sendChatComponent(uuid, message)
            }
            "SendProxyChannel" -> {
                val id = data[1]
                val channel = data[2]
                Loader.loadChannel(id, YamlConfiguration().also { it.loadFromString(channel) }).let {
                    Channel.channels[it.id] = it
                }
                if (Bukkit.getOnlinePlayers().isNotEmpty()) {
                    BukkitProxyManager.sendTrChatMessage(Bukkit.getOnlinePlayers().iterator().next(), "LoadedProxyChannel", id)
                }
            }
        }
    }

    class BungeeSide : BukkitProxyProcessor {

        private val TRCHAT_CHANNEL = "trchat:main"
        private val BUNGEE_CHANNEL = "BungeeCord"

        override fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChatBukkit.plugin, BUNGEE_CHANNEL)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChatBukkit.plugin, BUNGEE_CHANNEL)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(TrChatBukkit.plugin, BUNGEE_CHANNEL)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChatBukkit.plugin, BUNGEE_CHANNEL, BungeeSide())
            }
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChatBukkit.plugin, TRCHAT_CHANNEL)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChatBukkit.plugin, TRCHAT_CHANNEL)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(TrChatBukkit.plugin, TRCHAT_CHANNEL)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChatBukkit.plugin, TRCHAT_CHANNEL, BungeeSide())
            }
            submit(period = 60, async = true) {
                if (Bukkit.getOnlinePlayers().isNotEmpty()) {
                    sendCommonMessage(Bukkit.getOnlinePlayers().iterator().next(), "PlayerList", "ALL", async = false)
                }
            }
        }

        override fun sendCommonMessage(recipient: PluginMessageRecipient, vararg args: String, async: Boolean): Boolean {
            var success = true
            submit(async = async) {
                val out = ByteStreams.newDataOutput()

                try {
                    for (arg in args) {
                        out.writeUTF(arg)
                    }
                } catch (e: IOException) {
                    e.print("Failed to send proxy common message!")
                    success = false
                }

                recipient.sendPluginMessage(TrChatBukkit.plugin, BUNGEE_CHANNEL, out.toByteArray())
            }

            return success
        }

        override fun sendTrChatMessage(recipient: PluginMessageRecipient, vararg args: String, async: Boolean): Boolean {
            var success = true
            submit(async = async) {
                try {
                    for (bytes in buildMessage(*args)) {
                        recipient.sendPluginMessage(TrChatBukkit.plugin, TRCHAT_CHANNEL, bytes)
                    }
                } catch (e: IOException) {
                    e.print("Failed to send proxy trchat message!")
                    success = false
                }
            }

            return success
        }

        override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
            if (channel == BUNGEE_CHANNEL) {
                try {
                    val data = ByteStreams.newDataInput(message)
                    val subChannel = data.readUTF()
                    if (subChannel == "PlayerList") {
                        data.readUTF() // server
                        BukkitPlayers.setPlayers(data.readUTF().split(", "))
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
    }

    class VelocitySide : BukkitProxyProcessor {

        private val INCOMING_CHANNEL = "trchat:server"
        private val OUTGOING_CHANNEL = "trchat:proxy"

        override fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChatBukkit.plugin, OUTGOING_CHANNEL)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChatBukkit.plugin, OUTGOING_CHANNEL)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(TrChatBukkit.plugin, INCOMING_CHANNEL)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChatBukkit.plugin, INCOMING_CHANNEL, VelocitySide())
            }
        }

        override fun sendCommonMessage(recipient: PluginMessageRecipient, vararg args: String, async: Boolean): Boolean {
            error("Not supported.")
        }

        override fun sendTrChatMessage(recipient: PluginMessageRecipient, vararg args: String, async: Boolean): Boolean {
            var success = true
            submit(async = async) {
                try {
                    for (bytes in buildMessage(*args)) {
                        recipient.sendPluginMessage(TrChatBukkit.plugin, OUTGOING_CHANNEL, bytes)
                    }
                } catch (e: IOException) {
                    e.print("Failed to send proxy trchat message!")
                    success = false
                }
            }

            return success
        }

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

        companion object {


        }

    }
}