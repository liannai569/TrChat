package me.arasple.mc.trchat.module.display.function

import me.arasple.mc.trchat.module.internal.proxy.BukkitPlayers
import me.arasple.mc.trchat.util.*
import me.arasple.mc.trchat.util.color.colorify
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.replaceWithOrder
import taboolib.common5.mirrorNow
import taboolib.common5.util.parseMillis
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer

/**
 * @author wlys
 * @since 2022/3/18 19:14
 */
@StandardFunction
@PlatformSide([Platform.BUKKIT])
object Mention : Function("MENTION") {

    override val alias: String
        get() = "Mention"

    @ConfigNode("General.Mention.Enabled", "function.yml")
    var enabled = true

    @ConfigNode("General.Mention.Permission", "function.yml")
    var permission = "none"

    @ConfigNode("General.Mention.Format", "function.yml")
    var format = "&8[&3{0} &bx{1}&8]"

    @ConfigNode("General.Mention.Notify", "function.yml")
    var notify = true

    @ConfigNode("General.Mention.Self-Mention", "function.yml")
    var selfMention = false

    @ConfigNode("General.Mention.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    override fun createVariable(sender: Player, message: String): String {
        return mirrorNow("Function:Mention:ReplaceMessage") {
            if (!enabled) {
                message
            } else {
                var result = message
                var mentioned = false
                BukkitPlayers.getRegex(sender).forEach { regex ->
                    if (result.contains(regex) && !sender.isInCooldown(CooldownType.MENTION)) {
                        result = regex.replace(result, "{{MENTION:\$1}}")
                        mentioned = true
                    }
                }
                if (mentioned && !sender.hasPermission("trchat.bypass.mentioncd")) {
                    sender.updateCooldown(CooldownType.MENTION, cooldown.get())
                }
                result
            }
        }
    }

    override fun parseVariable(sender: Player, forward: Boolean, arg: String): Component {
        return mirrorNow("Function:Mention:CreateCompoennt") {
            if (notify && forward) {
                sender.sendProxyLang(arg, "Mentions-Notify", sender.name)
            }
            legacy(format.replaceWithOrder(arg).colorify())
        }
    }

    override fun canUse(sender: Player): Boolean {
        return sender.passPermission(permission)
    }

}