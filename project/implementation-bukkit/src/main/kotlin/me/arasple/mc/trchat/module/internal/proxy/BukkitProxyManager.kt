package me.arasple.mc.trchat.module.internal.proxy

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

/**
 * @author wlys
 * @since 2022/6/18 15:43
 */
@PlatformSide([Platform.BUKKIT])
object BukkitProxyManager : ProxyManager {

    init {
        PlatformFactory.registerAPI<ProxyManager>(this)
    }

    var processor: BukkitProxyProcessor? = null

    val platform by unsafeLazy {
        val force = Settings.CONF.getString("Options.Proxy")?.uppercase()
        if (Bukkit.getServer().spigot().config.getBoolean("settings.bungeecord") || force == "BUNGEE") {
            processor = BukkitProxyProcessor.BungeeSide.also { it.init() }
            console().sendLang("Plugin-Proxy-Supported", "Bungee")
            Platform.BUNGEE
        } else if (kotlin.runCatching { Bukkit.getServer().spigot().paperConfig.getBoolean("settings.velocity-support.enabled") }.getOrDefault(false) || force == "VELOCITY") {
            processor = BukkitProxyProcessor.VelocitySide.also { it.init() }
            console().sendLang("Plugin-Proxy-Supported", "Velocity")
            Platform.VELOCITY
        } else {
            console().sendLang("Plugin-Proxy-None")
            Platform.UNKNOWN
        }
    }

    override fun sendCommonMessage(recipient: Any, vararg args: String, async: Boolean): Boolean {
        if (recipient !is PluginMessageRecipient) return false
        return processor?.sendCommonMessage(recipient, *args, async = async) ?: false
    }

    override fun sendTrChatMessage(recipient: Any, vararg args: String, async: Boolean): Boolean {
        if (recipient !is PluginMessageRecipient) return false
        return processor?.sendTrChatMessage(recipient, *args, async = async) ?: false
    }

    fun sendProxyLang(player: Player, target: String, node: String, vararg args: String) {
        if (processor == null || Bukkit.getPlayerExact(target) != null) {
            getProxyPlayer(target)?.sendLang(node, *args)
        } else {
            sendTrChatMessage(player, "SendLang", target, node, *args)
        }
    }

}