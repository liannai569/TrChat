package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.internal.BukkitComponentManager
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.module.internal.data.Database
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyManager
import me.arasple.mc.trchat.module.internal.script.Condition
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageRecipient
import taboolib.common.platform.ProxyCommandSender
import taboolib.library.configuration.ConfigurationSection
import taboolib.platform.util.sendLang
import java.util.*

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

fun CommandSender.sendChatComponent(sender: Player, component: Component) {
    sendChatComponent(sender.uniqueId, component)
}

fun CommandSender.sendChatComponent(uuid: UUID, component: Component) {
    if (!HookPlugin.getInteractiveChat().sendMessage(this, component)) {
        if (TrChatBukkit.paperEnv) {
            sendMessage(Identity.identity(uuid), component, MessageType.CHAT)
        } else {
            BukkitComponentManager.getAudienceProvider().sender(this).sendMessage(Identity.identity(uuid), component, MessageType.CHAT)
        }
    }
}

fun ProxyCommandSender.sendChatComponent(sender: Player, component: Component) {
    cast<CommandSender>().sendChatComponent(sender, component)
}

fun ProxyCommandSender.sendChatComponent(uuid: UUID, component: Component) {
    cast<CommandSender>().sendChatComponent(uuid, component)
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