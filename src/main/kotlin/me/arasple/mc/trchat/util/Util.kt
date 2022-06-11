package me.arasple.mc.trchat.util

import com.google.gson.JsonParser
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.internal.data.Database
import me.arasple.mc.trchat.module.internal.script.Condition
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.text.SimpleDateFormat
import java.util.*

/**
 * Util
 * me.arasple.mc.trchat.util
 *
 * @author wlys
 * @since 2021/9/12 18:11
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object Util {

    lateinit var adventure: BukkitAudiences
        private set

    fun init() {
        adventure = BukkitAudiences.create(TrChat.plugin)
    }

    fun release() {
        adventure.close()
    }
}

val jsonParser = JsonParser()

val muteDateFormat = SimpleDateFormat()

fun Throwable.print(title: String, printStackTrace: Boolean = true) {
    console().sendMessage("§c[TrChat] §8$title")
    console().sendMessage("         §8${localizedMessage}")
    if (printStackTrace){
        stackTrace.forEach {
            console().sendMessage("         §8$it")
        }
    }
}

fun String.parseJson() = jsonParser.parse(this)

fun String.toCondition() = Condition(this)

fun Player.getSession() = ChatSession.getSession(this)

fun Player.checkMute(): Boolean {
    if (TrChat.isGlobalMuting && !hasPermission("trchat.bypass.globalmute")) {
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

fun CommandSender.sendProcessedMessage(sender: Player, component: Component) {
    sendProcessedMessage(sender.uniqueId, component)
}

fun CommandSender.sendProcessedMessage(uuid: UUID, component: Component) {
    if (TrChat.paperEnv) {
        sendMessage(Identity.identity(uuid), component, MessageType.CHAT)
    } else {
        Util.adventure.sender(this).sendMessage(Identity.identity(uuid), component, MessageType.CHAT)
    }
}

fun ProxyCommandSender.sendProcessedMessage(sender: Player, component: Component) {
    cast<CommandSender>().sendProcessedMessage(sender, component)
}

fun ProxyCommandSender.sendProcessedMessage(uuid: UUID, component: Component) {
    cast<CommandSender>().sendProcessedMessage(uuid, component)
}

fun Condition?.pass(commandSender: CommandSender): Boolean {
    return if (commandSender is Player) {
        this?.eval(commandSender) != false
    } else {
        true
    }
}

fun notify(notify: Array<out ProxyCommandSender>, node: String, vararg args: Any) {
    notify.forEach { it.sendLang(node, *args) }
}