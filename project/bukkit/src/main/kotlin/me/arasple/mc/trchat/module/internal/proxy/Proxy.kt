package me.arasple.mc.trchat.module.internal.proxy

import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.module.internal.proxy.bungee.Bungees
import me.arasple.mc.trchat.module.internal.proxy.velocity.Velocity
import me.arasple.mc.trchat.util.Internal
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageRecipient
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.module.lang.sendLang

/**
 * Proxy
 * me.arasple.mc.trchat.util.proxy
 *
 * @author wlys
 * @since 2021/8/21 13:24
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object Proxy {

    var isEnabled = false

    val platform by lazy {
        val force = Settings.CONF.getString("Options.Force-Proxy")?.uppercase()
        if (Bukkit.getServer().spigot().config.getBoolean("settings.bungeecord") || force == "BUNGEE") {
            isEnabled = true
            console().sendLang("Plugin-Proxy-Supported", "Bungee")
            Platform.BUNGEE
        } else if (kotlin.runCatching {
                Bukkit.getServer().spigot().paperConfig.getBoolean("settings.velocity-support.enabled")
        }.getOrDefault(false) || force == "VELOCITY") {
            isEnabled = true
            console().sendLang("Plugin-Proxy-Supported", "Velocity")
            Platform.VELOCITY
        } else {
            console().sendLang("Plugin-Proxy-None")
            Platform.UNKNOWN
        }
    }

    fun init() {
        when (platform) {
            Platform.BUNGEE -> Bungees.init()
            Platform.VELOCITY -> Velocity.init()
            else -> return
        }
    }

    fun sendBukkitMessage(recipient: PluginMessageRecipient, vararg args: String) {
        when (platform) {
            Platform.BUNGEE -> Bungees.sendBukkitMessage(recipient, *args)
            Platform.VELOCITY -> Velocity.sendBukkitMessage(recipient, *args)
            else -> return
        }
    }

    fun sendProxyLang(player: Player, target: String, node: String, vararg args: String) {
        if (!isEnabled || Bukkit.getPlayerExact(target) != null) {
            getProxyPlayer(target)?.sendLang(node, *args)
        } else {
            when (platform) {
                Platform.BUNGEE -> sendBukkitMessage(player, "SendLang", target, node, *args)
                Platform.VELOCITY -> sendBukkitMessage(player, "SendLang", target, node, *args)
                else -> return
            }
        }
    }
}

fun PluginMessageRecipient.sendBukkitMessage(vararg args: String) {
    Proxy.sendBukkitMessage(this, *args)
}

fun Player.sendProxyLang(target: String, node: String, vararg args: String) {
    Proxy.sendProxyLang(this, target, node, *args)
}