package me.arasple.mc.trchat.api.impl

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.velocitypowered.api.proxy.messages.ChannelMessageSink
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import com.velocitypowered.api.proxy.server.RegisteredServer
import me.arasple.mc.trchat.api.ProxyMessageManager
import me.arasple.mc.trchat.module.internal.TrChatVelocity.plugin
import me.arasple.mc.trchat.util.buildMessage
import me.arasple.mc.trchat.util.print
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Schedule
import taboolib.common.util.unsafeLazy
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * @author ItsFlicker
 * @since 2022/6/18 19:21
 */
@PlatformSide([Platform.VELOCITY])
object VelocityProxyManager : ProxyMessageManager {

    val incoming: MinecraftChannelIdentifier
    val outgoing: MinecraftChannelIdentifier

    init {
        PlatformFactory.registerAPI<ProxyMessageManager>(this)
        incoming = MinecraftChannelIdentifier.from("trchat:proxy")
        outgoing = MinecraftChannelIdentifier.from("trchat:server")
    }

    override val executor: ExecutorService by unsafeLazy {
        val factory = ThreadFactoryBuilder().setNameFormat("TrChat PluginMessage Processing Thread #%d").build()
        Executors.newFixedThreadPool(2, factory)
    }

    override val allNames = mutableMapOf<Int, Map<String, String?>>()

    override fun sendMessage(recipient: Any, vararg args: String): Future<*> {
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

    fun sendMessageToAll(vararg args: String, predicate: (RegisteredServer) -> Boolean = { true }) {
        plugin.server.allServers.filter { v -> predicate(v) }.forEach { v ->
            sendMessage(v, *args)
        }
    }

    @Schedule(async = true, period = 200L)
    override fun updateAllNames() {
        sendMessageToAll(
            "UpdateAllNames",
            allNames.values.joinToString(",") { it.keys.joinToString(",") },
            allNames.values.joinToString(",") { it.values.joinToString(",") }
        )
    }

}