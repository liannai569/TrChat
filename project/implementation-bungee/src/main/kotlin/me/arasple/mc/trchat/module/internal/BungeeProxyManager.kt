package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.ProxyManager
import me.arasple.mc.trchat.util.buildMessage
import me.arasple.mc.trchat.util.print
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.server
import taboolib.common.platform.function.submitAsync
import java.io.IOException
import java.util.concurrent.CompletableFuture

/**
 * @author wlys
 * @since 2022/6/18 19:21
 */
@PlatformSide([Platform.BUNGEE])
object BungeeProxyManager : ProxyManager {

    init {
        PlatformFactory.registerAPI<ProxyManager>(this)
        server<ProxyServer>().registerChannel(TrChatBungee.TRCHAT_CHANNEL)
    }

    override fun sendCommonMessage(recipient: Any, vararg args: String, async: Boolean): CompletableFuture<Boolean> {
        error("Not supported.")
    }

    override fun sendTrChatMessage(recipient: Any, vararg args: String, async: Boolean): CompletableFuture<Boolean> {
        if (recipient !is ServerInfo) {
            return CompletableFuture.completedFuture(false)
        }
        val success = CompletableFuture<Boolean>()
        fun send() {
            try {
                for (bytes in buildMessage(*args)) {
                    recipient.sendData(TrChatBungee.TRCHAT_CHANNEL, bytes)
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