package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.impl.BungeeChannelManager
import me.arasple.mc.trchat.api.impl.BungeeProxyManager
import me.arasple.mc.trchat.module.internal.TrChatBungee
import me.arasple.mc.trchat.util.print
import me.arasple.mc.trchat.util.proxy.common.MessageReader
import me.arasple.mc.trchat.util.toUUID
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.event.PluginMessageEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.server
import taboolib.common.util.subList
import taboolib.module.chat.Components
import taboolib.module.lang.sendLang
import java.io.IOException

/**
 * ListenerBungeeTransfer
 * me.arasple.mc.trchat.util.proxy.bungee
 *
 * @author ItsFlicker
 * @since 2021/8/9 15:01
 */
@PlatformSide([Platform.BUNGEE])
object ListenerBungeeTransfer {

    @SubscribeEvent(level = 0)
    fun onTransfer(e: PluginMessageEvent) {
        if (e.isCancelled) {
            return
        }
        if (e.tag == TrChatBungee.TRCHAT_CHANNEL) {
            try {
                val message = MessageReader.read(e.data)
                if (message.isCompleted) {
                    val data = message.build()
                    execute(data, e.sender)
                }
            } catch (ex: IOException) {
                ex.print("Error occurred while reading plugin message.")
            }
        }
    }

    @Suppress("Deprecation")
    private fun execute(data: Array<String>, connection: Connection) {
        when (data[0]) {
            "SendLang" -> {
                val to = data[1]
                val node = data[2]
                val args = subList(data.toList(), 3).toTypedArray()

                try {
                    getProxyPlayer(to)?.sendLang(node, *args)
                } catch (_: IllegalStateException) {
                }
            }
            "SendRaw" -> {
                val to = data[1]
                val raw = data[2]
                val player = getProxyPlayer(to) ?: return

                Components.parseRaw(raw).sendTo(player)
            }
            "BroadcastRaw" -> {
                val uuid = data[1]
                val raw = data[2]
                val perm = data[3]
                val doubleTransfer = data[4].toBoolean()
                val ports = data[5].takeIf { it != "" }?.split(";")?.map { it.toInt() }
                val message = Components.parseRaw(raw)

                if (doubleTransfer) {
                    BungeeProxyManager.sendMessageToAll("BroadcastRaw", uuid, raw, perm, data[4], data[5]) {
                        ports == null || it.address.port in ports
                    }
                } else {
                    server<ProxyServer>().servers.forEach { (_, v) ->
                        if (ports == null || v.address.port in ports) {
                            v.players.filter { perm == "" || it.hasPermission(perm) }.forEach {
                                it.sendMessage(uuid.toUUID(), message.toSpigotObject())
                            }
                        }
                    }
                }
                message.sendTo(console())
            }
            "UpdateNames" -> {
                val names = data[1].split(",").map { it.split("-", limit = 2) }
                BungeeProxyManager.allNames[connection.address.port] = names.associate { it[0] to it[1].takeIf { dn -> dn != "null" } }
            }
            "InventoryShow" -> {
                BungeeProxyManager.sendMessageToAll("InventoryShow", data[1], data[2], data[3], data[4])
            }
            "EnderChestShow" -> {
                BungeeProxyManager.sendMessageToAll("EnderChestShow", data[1], data[2], data[3], data[4])
            }
            "FetchProxyChannels" -> {
                BungeeChannelManager.sendAllProxyChannels(connection.address.port)
            }
            "LoadedProxyChannel" -> {
                val id = data[1]
                BungeeChannelManager.loadedServers.computeIfAbsent(id) { ArrayList() }.add(connection.address.port)
            }
        }
    }
}