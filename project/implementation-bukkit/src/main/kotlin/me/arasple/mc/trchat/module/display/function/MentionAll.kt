package me.arasple.mc.trchat.module.display.function

import me.arasple.mc.trchat.module.internal.proxy.BukkitPlayers
import me.arasple.mc.trchat.util.color.colorify
import me.arasple.mc.trchat.util.legacy
import me.arasple.mc.trchat.util.sendProxyLang
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common5.mirrorNow
import taboolib.common5.util.parseMillis
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer

/**
 * @author wlys
 * @since 2022/3/18 19:14
 */
@PlatformSide([Platform.BUKKIT])
object MentionAll {

    @ConfigNode("General.Mention-All.Enabled", "function.yml")
    var enabled = true

    @ConfigNode("General.Mention-All.Permission", "function.yml")
    var permission = "trchat.function.mentionall"

    @ConfigNode("General.Mention-All.Format", "function.yml")
    var format = "&8[&3{0} &bx{1}&8]"

    @ConfigNode("General.Mention-All.Notify", "function.yml")
    var notify = true

    @ConfigNode("General.Mention-All.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("General.Mention-All.Keys", "function.yml")
    var keys = emptyList<String>()

    fun replaceMessage(message: String): String {
        return if (!enabled) {
            message
        } else {
            var result = message
            keys.forEach {
                result = result.replace(it, "{{MENTIONALL}}")
            }
            result
        }
    }

    fun createComponent(player: Player, forward: Boolean): Component {
        return mirrorNow("Function:Mention:CreateCompoennt") {
            if (notify && forward) {
                BukkitPlayers.getPlayers().forEach {
                    player.sendProxyLang(it, "Mentions-Notify", player.name)
                }
            }
            legacy(format.colorify())
        }
    }
}