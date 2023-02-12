package me.arasple.mc.trchat.module.display.function.standard

import me.arasple.mc.trchat.module.conf.file.Functions
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.display.function.StandardFunction
import me.arasple.mc.trchat.module.internal.proxy.BukkitPlayers
import me.arasple.mc.trchat.module.internal.script.Reaction
import me.arasple.mc.trchat.util.*
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.asList
import taboolib.common.util.resettableLazy
import taboolib.common5.util.parseMillis
import taboolib.module.chat.ComponentText
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer

/**
 * @author ItsFlicker
 * @since 2022/3/18 19:14
 */
@StandardFunction
@PlatformSide([Platform.BUKKIT])
object Mention : Function("MENTION") {

    override val alias = "Mention"

    override val reaction by resettableLazy("functions") {
        Functions.CONF["General.Mention.Action"]?.let { Reaction(it.asList()) }
    }

    @ConfigNode("General.Mention.Enabled", "function.yml")
    var enabled = true

    @ConfigNode("General.Mention.Permission", "function.yml")
    var permission = "none"

    @ConfigNode("General.Mention.Notify", "function.yml")
    var notify = true

    @ConfigNode("General.Mention.Self-Mention", "function.yml")
    var selfMention = false

    @ConfigNode("General.Mention.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    override fun createVariable(sender: Player, message: String): String {
        return if (!enabled) {
            message
        } else {
            val regex = BukkitPlayers.getRegex(sender) ?: return message
            val result = regex.replace(message, "{{MENTION:\$1}}")
            if (result != message && !sender.hasPermission("trchat.bypass.mentioncd")) {
                sender.updateCooldown(CooldownType.MENTION, cooldown.get())
            }
            result
        }
    }

    override fun parseVariable(sender: Player, forward: Boolean, arg: String): ComponentText? {
        return mirrorParse {
            val name = BukkitPlayers.getPlayerFullName(arg) ?: arg
            if (notify && forward) {
                sender.sendProxyLang(name, "Function-Mention-Notify", sender.name)
            }
            sender.getComponentFromLang("Function-Mention-Format", name, sender.name)
        }
    }

    override fun canUse(sender: Player): Boolean {
        return sender.passPermission(permission) && !sender.isInCooldown(CooldownType.MENTION)
    }

    override fun checkCooldown(sender: Player, message: String): Boolean {
        return true
    }

}