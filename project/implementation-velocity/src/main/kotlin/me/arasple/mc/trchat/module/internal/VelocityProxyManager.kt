package me.arasple.mc.trchat.module.internal

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.velocitypowered.api.proxy.messages.ChannelMessageSink
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import me.arasple.mc.trchat.ProxyManager
import me.arasple.mc.trchat.util.buildMessage
import me.arasple.mc.trchat.util.print
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.util.unsafeLazy
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * @author wlys
 * @since 2022/6/18 19:21
 */
@PlatformSide([Platform.VELOCITY])
object VelocityProxyManager : ProxyManager {

    val incoming: MinecraftChannelIdentifier
    val outgoing: MinecraftChannelIdentifier

    override val executor: ExecutorService by unsafeLazy {
        val factory = ThreadFactoryBuilder().setNameFormat("TrChat PluginMessage Processing Thread #%d").build()
        Executors.newFixedThreadPool(2, factory)
    }

    init {
        PlatformFactory.registerAPI<ProxyManager>(this)
        incoming = MinecraftChannelIdentifier.from("trchat:proxy")
        outgoing = MinecraftChannelIdentifier.from("trchat:server")
    }

    override fun sendCommonMessage(recipient: Any, vararg args: String): Future<*> {
        error("Not supported.")
    }

    override fun sendTrChatMessage(recipient: Any, vararg args: String): Future<*> {
        if (recipient !is ChannelMessageSink) {
            return CompletableFuture.completedFuture(false)
        }
        return executor.submit {
            try {
                for (bytes in buildMessage(*args)) {
                    recipient.sendPluginMessage(outgoing, bytes)
                }
            } catch (e: IOException) {
                e.print("Failed to send proxy trchat message!")
            }
        }
    }

}