package me.arasple.mc.trchat.module.internal

import com.velocitypowered.api.proxy.messages.ChannelMessageSink
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import me.arasple.mc.trchat.ProxyManager
import me.arasple.mc.trchat.util.buildMessage
import me.arasple.mc.trchat.util.print
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submitAsync
import java.io.IOException
import java.util.concurrent.CompletableFuture

/**
 * @author wlys
 * @since 2022/6/18 19:21
 */
@PlatformSide([Platform.VELOCITY])
object VelocityProxyManager : ProxyManager {

    val incoming: MinecraftChannelIdentifier
    val outgoing: MinecraftChannelIdentifier

    init {
        PlatformFactory.registerAPI<ProxyManager>(this)
        incoming = MinecraftChannelIdentifier.from("trchat:proxy")
        outgoing = MinecraftChannelIdentifier.from("trchat:server")
    }

    @Schedule(async = true, period = 60)
    fun sendPlayerList() {
        val players = onlinePlayers().joinToString(", ") { it.name }
        TrChatVelocity.plugin.server.allServers.forEach { server ->
            sendTrChatMessage(
                server,
                "PlayerList",
                players,
                async = false
            )
        }
    }

    override fun sendCommonMessage(recipient: Any, vararg args: String, async: Boolean): CompletableFuture<Boolean> {
        error("Not supported.")
    }

    override fun sendTrChatMessage(recipient: Any, vararg args: String, async: Boolean): CompletableFuture<Boolean> {
        if (recipient !is ChannelMessageSink) {
            return CompletableFuture.completedFuture(false)
        }
        val success = CompletableFuture<Boolean>()
        fun send() {
            try {
                for (bytes in buildMessage(*args)) {
                    recipient.sendPluginMessage(outgoing, bytes)
                }
                success.complete(true)
            } catch (e: IOException) {
                e.print("Failed to send proxy trchat message!")
                success.complete(false)
            }
        }
        if (async) {
            submitAsync { send() }
        } else {
            send()
        }

        return success
    }

}