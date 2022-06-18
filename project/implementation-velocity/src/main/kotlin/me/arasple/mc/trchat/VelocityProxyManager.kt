package me.arasple.mc.trchat

import com.velocitypowered.api.proxy.messages.ChannelMessageSink
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import me.arasple.mc.trchat.util.buildMessage
import me.arasple.mc.trchat.util.print
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit
import java.io.IOException

/**
 * @author wlys
 * @since 2022/6/18 19:21
 */
@PlatformSide([Platform.VELOCITY])
object VelocityProxyManager : ProxyManager {

    var incoming: MinecraftChannelIdentifier
    var outgoing: MinecraftChannelIdentifier

    init {
        PlatformFactory.registerAPI<ProxyManager>(this)
        incoming = MinecraftChannelIdentifier.create("trchat", "proxy").also {
            TrChatVelocity.plugin.server.channelRegistrar.register(it)
        }
        outgoing = MinecraftChannelIdentifier.create("trchat", "server").also {
            TrChatVelocity.plugin.server.channelRegistrar.register(it)
        }
        submit(period = 60, async = true) {
            TrChatVelocity.plugin.server.allServers.forEach { server ->
                sendTrChatMessage(server, "PlayerList", onlinePlayers().joinToString(", ") { it.name })
            }
        }
    }

    override fun sendCommonMessage(recipient: Any, vararg args: String, async: Boolean): Boolean {
        error("Not supported.")
    }

    override fun sendTrChatMessage(recipient: Any, vararg args: String, async: Boolean): Boolean {
        if (recipient !is ChannelMessageSink) return false
        var success = true
        submit(async = async) {
            try {
                for (bytes in buildMessage(*args)) {
                    recipient.sendPluginMessage(outgoing, bytes)
                }
            } catch (e: IOException) {
                e.print("Failed to send proxy trchat message!")
                success = false
            }
        }

        return success
    }

    override fun getPlayers(): List<String> {
        return onlinePlayers().map { it.name }
    }

}