package me.arasple.mc.trchat.module.internal.listener

import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ServerConnection
import me.arasple.mc.trchat.api.impl.VelocityChannelManager
import me.arasple.mc.trchat.api.impl.VelocityProxyManager
import me.arasple.mc.trchat.module.internal.TrChatVelocity.plugin
import me.arasple.mc.trchat.util.print
import me.arasple.mc.trchat.util.proxy.common.MessageReader
import me.arasple.mc.trchat.util.toUUID
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.util.subList
import taboolib.module.lang.sendLang
import java.io.IOException

/**
 * ListenerVelocityTransfer
 * me.arasple.mc.trchat.util.proxy.velocity
 *
 * @author ItsFlicker
 * @since 2021/8/21 13:29
 */
@PlatformSide([Platform.VELOCITY])
object ListenerVelocityTransfer {

    @SubscribeEvent
    fun onTransfer(e: PluginMessageEvent) {
        if (e.identifier.id == VelocityProxyManager.incoming.id) {
            try {
                val message = MessageReader.read(e.data)
                if (message.isCompleted) {
                    val data = message.build()
                    execute(data, e.source as ServerConnection)
                }
            } catch (ex: IOException) {
                ex.print("Error occurred while reading plugin message.")
            }
        }
    }

    private fun execute(data: Array<String>, connection: ServerConnection) {
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
                val player = getProxyPlayer(to)?.cast<Player>() ?: return

                player.sendMessage(GsonComponentSerializer.gson().deserialize(raw))
            }
            "BroadcastRaw" -> {
                val uuid = data[1]
                val raw = data[2]
                val perm = data[3]
                val doubleTransfer = data[4].toBoolean()
                val ports = data[5].takeIf { it != "" }?.split(";")?.map { it.toInt() }
                val message = GsonComponentSerializer.gson().deserialize(raw)

                if (doubleTransfer) {
                    VelocityProxyManager.sendMessageToAll("BroadcastRaw", uuid, raw, perm, data[4], data[5]) {
                        ports == null || it.serverInfo.address.port in ports
                    }
                } else {
                    plugin.server.allServers.forEach { server ->
                        if (ports == null || server.serverInfo.address.port in ports) {
                            server.playersConnected.filter { perm == "" || it.hasPermission(perm) }.forEach {
                                it.sendMessage(Identity.identity(uuid.toUUID()), message, MessageType.CHAT)
                            }
                        }
                    }
                }
                plugin.server.consoleCommandSource.sendMessage(message)
            }
            "UpdateNames" -> {
                val names = data[1].split(",").map { it.split("-", limit = 2) }
                VelocityProxyManager.allNames[connection.serverInfo.address.port] = names.associate { it[0] to it[1].takeIf { dn -> dn != "null" } }
            }
            "InventoryShow" -> {
                VelocityProxyManager.sendMessageToAll("InventoryShow", data[1], data[2], data[3], data[4])
            }
            "EnderChestShow" -> {
                VelocityProxyManager.sendMessageToAll("EnderChestShow", data[1], data[2], data[3], data[4])
            }
            "FetchProxyChannels" -> {
                VelocityChannelManager.sendAllProxyChannels(connection.serverInfo.address.port)
            }
            "LoadedProxyChannel" -> {
                val id = data[1]
                VelocityChannelManager.loadedServers.computeIfAbsent(id) { ArrayList() }.add(connection.serverInfo.address.port)
            }
        }
    }
}