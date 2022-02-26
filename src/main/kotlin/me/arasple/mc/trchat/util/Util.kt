package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.internal.data.Database
import me.arasple.mc.trchat.module.internal.script.Condition
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.util.concurrent.TimeUnit

/**
 * Util
 * me.arasple.mc.trchat.util
 *
 * @author wlys
 * @since 2021/9/12 18:11
 */
fun Throwable.print(title: String) {
    println("§c[TrChat] §8$title")
    println("         §8${localizedMessage}")
    stackTrace.forEach {
        println("         §8$it")
    }
}

fun String.toCondition() = Condition(this)

fun Player.getSession() = ChatSession.getSession(this)

fun Player.checkMute(): Boolean {
    if (TrChat.isGlobalMuting && !hasPermission("trchat.bypass.globalmute")) {
        sendLang("General-Global-Muting")
        return false
    }
    if (this.getSession().isMuted) {
        sendLang("General-Muted", TimeUnit.MILLISECONDS.toSeconds(getDataContainer().getLong("mute_time")))
        return false
    }
    return true
}

fun Player.getDataContainer(): ConfigurationSection {
    return Database.database.pull(this)
}

fun Condition?.pass(player: Player): Boolean {
    return this?.eval(player) != false
}

fun notify(notify: Array<out ProxyCommandSender>, node: String, vararg args: Any) {
    notify.forEach {
        it.sendLang(node, *args)
    }
}