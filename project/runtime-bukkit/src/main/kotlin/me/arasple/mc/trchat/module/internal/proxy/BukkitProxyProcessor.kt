package me.arasple.mc.trchat.module.internal.proxy

import com.google.common.io.ByteStreams
import me.arasple.mc.trchat.api.impl.BukkitProxyManager
import me.arasple.mc.trchat.module.conf.Loader
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.display.function.standard.EnderChestShow
import me.arasple.mc.trchat.module.display.function.standard.InventoryShow
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.util.*
import me.arasple.mc.trchat.util.proxy.common.MessageReader
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.plugin.messaging.PluginMessageRecipient
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submitAsync
import taboolib.common5.util.decodeBase64
import taboolib.module.chat.Components
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.util.bukkitPlugin
import taboolib.platform.util.deserializeToInventory
import taboolib.platform.util.onlinePlayers
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * @author ItsFlicker
 * @since 2022/6/18 16:17
 */
sealed interface BukkitProxyProcessor : PluginMessageListener {

    operator fun invoke(): BukkitProxyProcessor {
        submitAsync(delay = 100, period = 60) {
            if (onlinePlayers.isNotEmpty()) {
                BukkitProxyManager.sendCommonMessage(onlinePlayers.iterator().next(), "PlayerList", "ALL")
            }
        }
        return this
    }

    fun close() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(bukkitPlugin)
    }

    fun sendCommonMessage(
        recipient: PluginMessageRecipient,
        executor: ExecutorService,
        vararg args: String
    ): Future<*> {
        return executor.submit {
            val out = ByteStreams.newDataOutput()
            try {
                for (arg in args) {
                    out.writeUTF(arg)
                }
                recipient.sendPluginMessage(bukkitPlugin, BUNGEE_CHANNEL, out.toByteArray())
            } catch (e: IOException) {
                e.print("Failed to send proxy common message!")
            }
        }
    }

    fun sendTrChatMessage(
        recipient: PluginMessageRecipient,
        executor: ExecutorService,
        vararg args: String
    ): Future<*>

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
                val message = Components.parseRaw(raw)

                if (permission == "") {
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
            "InventoryShow" -> {
                if (data[1] == MinecraftVersion.minecraftVersion) {
                    val name = data[2]
                    val sha1 = data[3]
                    if (InventoryShow.cache.getIfPresent(sha1) == null) {
                        val inventory = data[4].decodeBase64().deserializeToInventory(createNoClickInventory(54, "$name's Inventory"))
                        InventoryShow.cache.put(sha1, inventory)
                    }
                }
            }
            "EnderChestShow" -> {
                if (data[1] == MinecraftVersion.minecraftVersion) {
                    val name = data[2]
                    val sha1 = data[3]
                    if (EnderChestShow.cache.getIfPresent(sha1) == null) {
                        val inventory = data[4].decodeBase64().deserializeToInventory(createNoClickInventory(27, "$name's Ender Chest"))
                        EnderChestShow.cache.put(sha1, inventory)
                    }
                }
            }
        }
    }

    object BungeeSide : BukkitProxyProcessor {

        private const val TRCHAT_CHANNEL = "trchat:main"

        override operator fun invoke(): BukkitProxyProcessor {
            BUNGEE_CHANNEL.registerOutgoing()
            BUNGEE_CHANNEL.registerIncoming(this)
            TRCHAT_CHANNEL.registerOutgoing()
            TRCHAT_CHANNEL.registerIncoming(this)
            return super.invoke()
        }

        override fun sendTrChatMessage(
            recipient: PluginMessageRecipient,
            executor: ExecutorService,
            vararg args: String
        ): Future<*> {
            return executor.submit {
                try {
                    for (bytes in buildMessage(*args)) {
                        recipient.sendPluginMessage(bukkitPlugin, TRCHAT_CHANNEL, bytes)
                    }
                } catch (e: IOException) {
                    e.print("Failed to send proxy trchat message!")
                }
            }
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
                } catch (e: IOException) {
                    e.print("Failed to read proxy common message!")
                }
            }
            if (channel == TRCHAT_CHANNEL) {
                try {
                    val data = MessageReader.read(message)
                    if (data.isCompleted) {
                        execute(data.build())
                    }
                } catch (e: IOException) {
                    e.print("Failed to read proxy trchat message!")
                }
            }
        }
    }

    object VelocitySide : BukkitProxyProcessor {

        private const val TRCHAT_INCOMING = "trchat:server"
        private const val TRCHAT_OUTGOING = "trchat:proxy"

        override operator fun invoke(): BukkitProxyProcessor {
            BUNGEE_CHANNEL.registerOutgoing()
            BUNGEE_CHANNEL.registerIncoming(this)
            TRCHAT_OUTGOING.registerOutgoing()
            TRCHAT_INCOMING.registerIncoming(this)
            return super.invoke()
        }

        override fun sendTrChatMessage(
            recipient: PluginMessageRecipient,
            executor: ExecutorService,
            vararg args: String
        ): Future<*> {
            return executor.submit {
                try {
                    for (bytes in buildMessage(*args)) {
                        recipient.sendPluginMessage(bukkitPlugin, TRCHAT_OUTGOING, bytes)
                    }
                } catch (e: IOException) {
                    e.print("Failed to send proxy trchat message!")
                }
            }
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
                } catch (e: IOException) {
                    e.print("Failed to read proxy common message!")
                }
            }
            if (channel == TRCHAT_INCOMING) {
                try {
                    val data = MessageReader.read(message)
                    if (data.isCompleted) {
                        execute(data.build())
                    }
                } catch (e: IOException) {
                    e.print("Failed to read proxy trchat message!")
                }
            }
        }
    }

    companion object {

        protected const val BUNGEE_CHANNEL = "BungeeCord"

        protected fun String.registerOutgoing() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(bukkitPlugin, this)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(bukkitPlugin, this)
            }
        }

        protected fun String.registerIncoming(listener: PluginMessageListener) {
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(bukkitPlugin, this)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(bukkitPlugin, this, listener)
            }
        }

    }

}