package me.arasple.mc.trchat.module.internal.proxy

import com.google.common.base.Enums
import me.arasple.mc.trchat.ProxyManager
import me.arasple.mc.trchat.module.conf.file.Settings
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageRecipient
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.util.unsafeLazy
import taboolib.module.lang.sendLang
import java.util.concurrent.CompletableFuture

/**
 * @author wlys
 * @since 2022/6/18 15:43
 */
@PlatformSide([Platform.BUKKIT])
object BukkitProxyManager : ProxyManager {

    init {
        PlatformFactory.registerAPI<ProxyManager>(this)
    }

    val processor by unsafeLazy {
        when (platform) {
            Platform.BUNGEE -> BukkitProxyProcessor.BungeeSide.also { it.init() }
            Platform.VELOCITY -> BukkitProxyProcessor.VelocitySide.also { it.init() }
            else -> null
        }
    }

    val platform: Platform by unsafeLazy {
        val force = Enums.getIfPresent(Platform::class.java, Settings.CONF.getString("Options.Proxy")?.uppercase() ?: "AUTO")
        if (force.isPresent) {
            force.get()
        } else {
            if (Bukkit.getServer().spigot().config.getBoolean("settings.bungeecord")) {
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

    override fun sendCommonMessage(recipient: Any, vararg args: String, async: Boolean): CompletableFuture<Boolean> {
        if (processor == null || recipient !is PluginMessageRecipient) return CompletableFuture.completedFuture(false)
        return processor!!.sendCommonMessage(recipient, *args, async = async)
    }

    override fun sendTrChatMessage(recipient: Any, vararg args: String, async: Boolean): CompletableFuture<Boolean> {
        if (processor == null || recipient !is PluginMessageRecipient) return CompletableFuture.completedFuture(false)
        return processor!!.sendTrChatMessage(recipient, *args, async = async)
    }

    fun sendProxyLang(player: Player, target: String, node: String, vararg args: String) {
        if (processor == null || Bukkit.getPlayerExact(target) != null) {
            getProxyPlayer(target)?.sendLang(node, *args)
        } else {
            sendTrChatMessage(player, "SendLang", target, node, *args)
        }
    }

}