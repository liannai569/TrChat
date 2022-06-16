package me.arasple.mc.trchat.module.display.function

import me.arasple.mc.trchat.module.internal.proxy.bukkit.Players
import me.arasple.mc.trchat.module.internal.proxy.sendProxyLang
import me.arasple.mc.trchat.util.CooldownType
import me.arasple.mc.trchat.util.color.colorify
import me.arasple.mc.trchat.util.isInCooldown
import me.arasple.mc.trchat.util.legacy
import me.arasple.mc.trchat.util.updateCooldown
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
@PlatformSide([Platform.BUKKIT])
object Mention {

    @ConfigNode("General.Mention.Enabled", "function.yml")
    var enabled = true

    @ConfigNode("General.Mention.Permission", "function.yml")
    var permission = "null"

    @ConfigNode("General.Mention.Format", "function.yml")
    var format = "&8[&3{0} &bx{1}&8]"

    @ConfigNode("General.Mention.Notify", "function.yml")
    var notify = true

    @ConfigNode("General.Mention.Self-Mention", "function.yml")
    var selfMention = false

    @ConfigNode("General.Mention.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    fun replaceMessage(message: String, player: Player): String {
        return mirrorNow("Function:Mention:ReplaceMessage") {
            if (!enabled) {
                message
            } else {
                var result = message
                var mentioned = false
                Players.getRegex(player).forEach { regex ->
                    if (result.contains(regex) && !player.isInCooldown(CooldownType.MENTION)) {
                        result = regex.replace(result, "{{MENTION:\$1}}")
                        mentioned = true
                    }
                }
                if (mentioned && !player.hasPermission("trchat.bypass.mentioncd")) {
                    player.updateCooldown(CooldownType.MENTION, cooldown.get())
                }
                result
            }
        }
    }

    fun createComponent(player: Player, target: String): Component {
        return mirrorNow("Function:Mention:CreateCompoennt") {
            if (notify) {
                player.sendProxyLang(target, "Mentions-Notify", player.name)
            }
            legacy(format.replaceWithOrder(target).colorify())
        }
    }
}