package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.internal.BukkitComponentManager
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.module.internal.command.main.CommandMute
import me.arasple.mc.trchat.module.internal.data.Databases
import me.arasple.mc.trchat.module.internal.data.PlayerData
import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyManager
import me.arasple.mc.trchat.module.internal.script.Condition
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageRecipient
import taboolib.common.util.unsafeLazy
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.nms.nmsClass
import taboolib.module.ui.MenuHolder
import taboolib.module.ui.type.Basic
import taboolib.platform.util.sendLang

val classChatSerializer by unsafeLazy { nmsClass("IChatBaseComponent\$ChatSerializer") }
val isDragonCoreHooked by unsafeLazy { Bukkit.getPluginManager().isPluginEnabled("DragonCore") }
private val noClickBasic = object : Basic() {
    init {
        rows(6)
        onClick(lock = true)
    }
}

@Suppress("Deprecation")
fun createNoClickInventory(size: Int, title: String) =
    Bukkit.createInventory(MenuHolder(noClickBasic), size, title)

fun String?.toCondition() = if (this == null) Condition.EMPTY else Condition(this)

fun Condition?.pass(commandSender: CommandSender): Boolean {
    return if (commandSender is Player) {
        this?.eval(commandSender) != false
    } else {
        true
    }
}

fun Player.passPermission(permission: String?): Boolean {
    return permission.isNullOrEmpty()
            || permission.equals("null", ignoreCase = true)
            || permission.equals("none", ignoreCase = true)
            || hasPermission(permission)
}

inline val Player.session get() = ChatSession.getSession(this)

inline val OfflinePlayer.data get() = PlayerData.getData(this)

fun Player.checkMute(): Boolean {
    if (TrChatBukkit.isGlobalMuting && !hasPermission("trchat.bypass.globalmute")) {
        sendLang("General-Global-Muting")
        return false
    }
    val data = data
    if (data.isMuted) {
        sendLang("General-Muted", CommandMute.muteDateFormat.format(data.muteTime), data.muteReason)
        return false
    }
    return true
}

fun OfflinePlayer.getDataContainer(): ConfigurationSection {
    return Databases.database.pull(this)
}

fun Any.sendComponent(sender: Any?, component: Component) {
    when (val method = Settings.CONF.getString("Options.Send-Message-Method", "CHAT")!!.uppercase()) {
        "CHAT" -> BukkitComponentManager.sendChatComponent(this, component, sender)
        "SYSTEM" -> BukkitComponentManager.sendSystemComponent(this, component, sender)
        else -> error("Unsupported send message method $method.")
    }
}

fun PluginMessageRecipient.sendTrChatMessage(vararg args: String) {
    BukkitProxyManager.sendTrChatMessage(this, *args)
}

fun Player.sendProxyLang(target: String, node: String, vararg args: String) {
    BukkitProxyManager.sendProxyLang(this, target, node, *args)
}

fun Player.getCooldownLeft(type: CooldownType) = Cooldowns.getCooldownLeft(uniqueId, type.alias)

fun Player.isInCooldown(type: CooldownType) = Cooldowns.isInCooldown(uniqueId, type.alias)

fun Player.updateCooldown(type: CooldownType, lasts: Long) = Cooldowns.updateCooldown(uniqueId, type.alias, lasts)