package me.arasple.mc.trchat

import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.proxy.Player
import me.arasple.mc.trchat.TrChatVelocity.plugin
import me.arasple.mc.trchat.util.proxy.common.MessageReader
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
import java.util.*

/**
 * ListenerVelocityTransfer
 * me.arasple.mc.trchat.util.proxy.velocity
 *
 * @author wlys
 * @since 2021/8/21 13:29
 */
@PlatformSide([Platform.VELOCITY])
object ListenerVelocityTransfer {

    @SubscribeEvent(ignoreCancelled = true)
    fun onTransfer(e: PluginMessageEvent) {
        if (e.identifier == VelocityProxyManager.incoming) {
            try {
                val message = MessageReader.read(e.data)
                if (message.isCompleted) {
                    val data = message.build()
                    execute(data)
                }
            } catch (_: IOException) {
            }
        }
    }

    private fun execute(data: Array<String>) {
        when (data[0]) {
            "SendRaw" -> {
                val to = data[1]
                val raw = data[2]
                val player = getProxyPlayer(to)?.cast<Player>() ?: return
                val message = GsonComponentSerializer.gson().deserialize(raw)

                player.sendMessage(message)

            }
            "BroadcastRaw" -> {
                val uuid = data[1]
                val raw = data[2]
                val permission = data[3]
                val doubleTransfer = data[4].toBoolean()
                val message = GsonComponentSerializer.gson().deserialize(raw)

                if (doubleTransfer) {
                    plugin.server.allServers.forEach {
                        VelocityProxyManager.sendTrChatMessage(it, "BroadcastRaw", uuid, raw, permission)
                    }
                } else {
                    plugin.server.allServers.forEach { server ->
                        server.playersConnected.filter { permission == "null" || it.hasPermission(permission) }.forEach { player ->
                            player.sendMessage(Identity.identity(UUID.fromString(uuid)), message, MessageType.CHAT)
                        }
                    }
                }

                plugin.server.consoleCommandSource.sendMessage(message)
            }
            "ForwardRaw" -> {
                val uuid = data[1]
                val raw = data[2]
                val permission = data[3]
                val ports = data[4].split(";").map { it.toInt() }
                val doubleTransfer = data[5].toBoolean()
                val message = GsonComponentSerializer.gson().deserialize(raw)

                if (doubleTransfer) {
                    plugin.server.allServers.forEach {
                        if (ports.contains(it.serverInfo.address.port)) {
                            VelocityProxyManager.sendTrChatMessage(it, "BroadcastRaw", uuid, raw, permission)
                        }
                    }
                } else {
                    plugin.server.allServers.forEach { server ->
                        if (ports.contains(server.serverInfo.address.port)) {
                            server.playersConnected.filter { permission == "null" || it.hasPermission(permission) }.forEach { player ->
                                player.sendMessage(Identity.identity(UUID.fromString(uuid)), message, MessageType.CHAT)
                            }
                        }
                    }
                }

                plugin.server.consoleCommandSource.sendMessage(message)
            }
            "SendLang" -> {
                val to = data[1]
                val node = data[2]
                val args = subList(data.toList(), 3).toTypedArray()

                try {
                    getProxyPlayer(to)?.sendLang(node, *args)
                } catch (_: IllegalStateException) {
                }
            }
        }
    }
}