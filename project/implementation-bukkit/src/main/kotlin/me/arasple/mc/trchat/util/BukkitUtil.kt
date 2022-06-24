package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.internal.BukkitComponentManager
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.module.internal.data.Database
import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyManager
import me.arasple.mc.trchat.module.internal.script.Condition
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageRecipient
import taboolib.library.configuration.ConfigurationSection
import taboolib.platform.util.sendLang

/**
 * @author wlys
 * @since 2022/6/8 12:48
 */
fun String.toCondition() = Condition(this)

fun Condition?.pass(commandSender: CommandSender): Boolean {
    return if (commandSender is Player) {
        this?.eval(commandSender) != false
    } else {
        true
    }
}

fun Player.getSession() = ChatSession.getSession(this)

fun Player.checkMute(): Boolean {
    if (TrChatBukkit.isGlobalMuting && !hasPermission("trchat.bypass.globalmute")) {
        sendLang("General-Global-Muting")
        return false
    }
    val session = getSession()
    if (session.isMuted) {
        sendLang("General-Muted", muteDateFormat.format(session.muteTime), session.muteReason)
        return false
    }
    return true
}

fun Player.getDataContainer(): ConfigurationSection {
    return Database.database.pull(this)
}

fun Any.sendComponent(sender: Any, component: Component) {
    when (val method = Settings.CONF.getString("Options.Send-Message-Method", "CHAT")!!.uppercase()) {
        "CHAT" -> BukkitComponentManager.sendChatComponent(this, component, sender)
        "SYSTEM" -> BukkitComponentManager.sendSystemComponent(this, component)
        else -> error("Unsupported send message method $method.")
    }
}

fun PluginMessageRecipient.sendTrChatMessage(vararg args: String) {
    BukkitProxyManager.sendTrChatMessage(this, *args)
}

fun Player.sendProxyLang(target: String, node: String, vararg args: String) {
    BukkitProxyManager.sendProxyLang(this, target, node, *args)
}

fun Player.getCooldownLeft(type: CooldownType) = Cooldowns.getCooldownLeft(uniqueId, type)

fun Player.isInCooldown(type: CooldownType) = Cooldowns.isInCooldown(uniqueId, type)

fun Player.updateCooldown(type: CooldownType, lasts: Long) = Cooldowns.updateCooldown(uniqueId, type, lasts)