package me.arasple.mc.trchat.api.impl

import com.google.common.util.concurrent.ThreadFactoryBuilder
import me.arasple.mc.trchat.api.ProxyManager
import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyProcessor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageRecipient
import org.spigotmc.SpigotConfig
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.util.unsafeLazy
import taboolib.module.lang.sendLang
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * @author ItsFlicker
 * @since 2022/6/18 15:43
 */
@PlatformSide([Platform.BUKKIT])
object BukkitProxyManager : ProxyManager {

    init {
        PlatformFactory.registerAPI<ProxyManager>(this)
    }

    override val executor: ExecutorService by unsafeLazy {
        val factory = ThreadFactoryBuilder().setNameFormat("TrChat PluginMessage Processing Thread #%d").build()
        Executors.newFixedThreadPool(2, factory)
    }

    val platform: Platform by unsafeLazy {
        val force = kotlin.runCatching {
            Platform.valueOf(Settings.conf.getString("Options.Proxy")?.uppercase() ?: "AUTO")
        }
        if (force.isSuccess) {
            force.getOrThrow()
        } else {
            if (SpigotConfig.bungee) {
                console().sendLang("Plugin-Proxy-Supported", "Bungee")
                Platform.BUNGEE
            } else if (kotlin.runCatching {
                    Bukkit.spigot().paperConfig.getBoolean("proxies.velocity.enabled", false) ||
                            Bukkit.spigot().paperConfig.getBoolean("settings.velocity-support.enabled", false)
                }.getOrDefault(false)) {
                console().sendLang("Plugin-Proxy-Supported", "Velocity")
                Platform.VELOCITY
            } else {
                console().sendLang("Plugin-Proxy-None")
                Platform.UNKNOWN
            }
        }
    }

    val processor by unsafeLazy {
        executor
        when (platform) {
            Platform.BUNGEE -> {
                BukkitProxyProcessor.BungeeSide()
            }
            Platform.VELOCITY -> {
                BukkitProxyProcessor.VelocitySide()
            }
            else -> null
        }
    }

    override fun sendCommonMessage(recipient: Any, vararg args: String): Future<*> {
        if (processor == null || recipient !is PluginMessageRecipient) return CompletableFuture.completedFuture(false)
        return processor!!.sendCommonMessage(recipient, executor, *args)
    }

    override fun sendTrChatMessage(recipient: Any, vararg args: String): Future<*> {
        if (processor == null || recipient !is PluginMessageRecipient) return CompletableFuture.completedFuture(false)
        return processor!!.sendTrChatMessage(recipient, executor, *args)
    }

    fun sendProxyLang(player: Player, target: String, node: String, vararg args: String) {
        if (processor == null || Bukkit.getPlayerExact(target) != null) {
            getProxyPlayer(target)?.sendLang(node, *args)
        } else {
            sendTrChatMessage(player, "SendLang", target, node, *args)
        }
    }

    override fun close() {
        processor?.close()
        executor.shutdownNow()
    }

}