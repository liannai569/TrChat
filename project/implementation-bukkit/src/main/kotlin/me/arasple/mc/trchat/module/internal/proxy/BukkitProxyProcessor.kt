package me.arasple.mc.trchat.module.internal.proxy

import com.google.common.io.ByteStreams
import me.arasple.mc.trchat.module.conf.Loader
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.util.buildMessage
import me.arasple.mc.trchat.util.print
import me.arasple.mc.trchat.util.proxy.common.MessageReader
import me.arasple.mc.trchat.util.sendComponent
import me.arasple.mc.trchat.util.toUUID
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.plugin.messaging.PluginMessageRecipient
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.platform.util.bukkitPlugin
import taboolib.platform.util.onlinePlayers
import java.io.IOException

/**
 * @author wlys
 * @since 2022/6/18 16:17
 */
sealed interface BukkitProxyProcessor : PluginMessageListener {

    fun init()

    fun close() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(bukkitPlugin)
    }

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
                val uuid = data[1].toUUID()
                val raw = data[2]
                val permission = data[3]
                val message = GsonComponentSerializer.gson().deserialize(raw)

                if (permission == "null") {
                    onlinePlayers.forEach { it.sendComponent(uuid, message) }
                } else {
                    onlinePlayers.filter { it.hasPermission(permission) }.forEach { it.sendComponent(uuid, message) }
                }
                console().sendComponent(uuid, message)
            }
            "SendProxyChannel" -> {
                val id = data[1]
                val channel = data[2]
                Loader.loadChannel(id, YamlConfiguration().also { it.loadFromString(channel) }).let {
                    Channel.channels[it.id] = it
                }
                if (onlinePlayers.isNotEmpty()) {
                    BukkitProxyManager.sendTrChatMessage(onlinePlayers.iterator().next(), "LoadedProxyChannel", id)
                }
            }
        }
    }

    object BungeeSide : BukkitProxyProcessor {

        private const val TRCHAT_CHANNEL = "trchat:main"
        private const val BUNGEE_CHANNEL = "BungeeCord"

        override fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(bukkitPlugin, BUNGEE_CHANNEL)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(bukkitPlugin, BUNGEE_CHANNEL)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(bukkitPlugin, BUNGEE_CHANNEL)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(bukkitPlugin, BUNGEE_CHANNEL, BungeeSide)
            }
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(bukkitPlugin, TRCHAT_CHANNEL)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(bukkitPlugin, TRCHAT_CHANNEL)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(bukkitPlugin, TRCHAT_CHANNEL)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(bukkitPlugin, TRCHAT_CHANNEL, BungeeSide)
            }
            submit(period = 60, async = true) {
                if (onlinePlayers.isNotEmpty()) {
                    sendCommonMessage(onlinePlayers.iterator().next(), "PlayerList", "ALL", async = false)
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

                recipient.sendPluginMessage(bukkitPlugin, BUNGEE_CHANNEL, out.toByteArray())
            }

            return success
        }

        override fun sendTrChatMessage(recipient: PluginMessageRecipient, vararg args: String, async: Boolean): Boolean {
            var success = true
            submit(async = async) {
                try {
                    for (bytes in buildMessage(*args)) {
                        recipient.sendPluginMessage(bukkitPlugin, TRCHAT_CHANNEL, bytes)
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

    object VelocitySide : BukkitProxyProcessor {

        private const val incoming = "trchat:server"
        private const val outgoing = "trchat:proxy"

        override fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(bukkitPlugin, outgoing)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(bukkitPlugin, outgoing)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(bukkitPlugin, incoming)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(bukkitPlugin, incoming, VelocitySide)
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
                        recipient.sendPluginMessage(bukkitPlugin, outgoing, bytes)
                    }
                } catch (e: IOException) {
                    e.print("Failed to send proxy trchat message!")
                    success = false
                }
            }

            return success
        }

        override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
            if (channel != incoming) {
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
    }

}