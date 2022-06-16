package me.arasple.mc.trchat

import me.arasple.mc.trchat.util.BungeeAdventure
import me.arasple.mc.trchat.util.buildMessage
import me.arasple.mc.trchat.util.proxy.common.MessageReader
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.server
import taboolib.common.util.subList
import taboolib.module.lang.sendLang
import java.io.IOException
import java.util.*

/**
 * ListenerBungeeTransfer
 * me.arasple.mc.trchat.util.proxy.bungee
 *
 * @author Arasple, wlys
 * @since 2021/8/9 15:01
 */
@PlatformSide([Platform.BUNGEE])
object ListenerBungeeTransfer {

    @SubscribeEvent(ignoreCancelled = true)
    fun onTransfer(e: PluginMessageEvent) {
        if (e.tag == TrChatBungee.TRCHAT_CHANNEL) {
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
                val player = getProxyPlayer(to)?.cast<ProxiedPlayer>() ?: return
                val message = GsonComponentSerializer.gson().deserialize(raw)

                if (player.isConnected) {
                    BungeeAdventure.adventure.player(player).sendMessage(message)
                }
            }
            "BroadcastRaw" -> {
                val uuid = data[1]
                val raw = data[2]
                val permission = data[3]
                val doubleTransfer = data[4].toBoolean()
                val message = GsonComponentSerializer.gson().deserialize(raw)

                if (doubleTransfer) {
                    server<ProxyServer>().servers.forEach { (_, v) ->
                        for (bytes in buildMessage("BroadcastRaw", uuid, raw, permission)) {
                            v.sendData(TrChatBungee.TRCHAT_CHANNEL, bytes)
                        }
                    }
                } else {
                    server<ProxyServer>().servers.forEach { (_, v) ->
                        v.players.filter { permission == "null" || it.hasPermission(permission) }.forEach {
                            BungeeAdventure.adventure.player(it).sendMessage(Identity.identity(UUID.fromString(uuid)), message, MessageType.CHAT)
                        }
                    }
                }

                BungeeAdventure.adventure.console().sendMessage(message)
            }
            "ForwardRaw" -> {
                val uuid = data[1]
                val raw = data[2]
                val permission = data[3]
                val ports = data[4].split(";").map { it.toInt() }
                val doubleTransfer = data[5].toBoolean()
                val message = GsonComponentSerializer.gson().deserialize(raw)

                if (doubleTransfer) {
                    server<ProxyServer>().servers.forEach { (_, v) ->
                        if (ports.contains(v.address.port)) {
                            for (bytes in buildMessage("BroadcastRaw", uuid, raw, permission)) {
                                v.sendData(TrChatBungee.TRCHAT_CHANNEL, bytes)
                            }
                        }
                    }
                } else {
                    server<ProxyServer>().servers.forEach { (_, v) ->
                        if (ports.contains(v.address.port)) {
                            v.players.filter { permission == "null" || it.hasPermission(permission) }.forEach {
                                BungeeAdventure.adventure.player(it).sendMessage(Identity.identity(UUID.fromString(uuid)), message, MessageType.CHAT)
                            }
                        }
                    }
                }

                BungeeAdventure.adventure.console().sendMessage(message)
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