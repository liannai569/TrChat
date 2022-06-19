package me.arasple.mc.trchat.module.internal.command.sub

import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.getSession
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.subCommand

/**
 * @author wlys
 * @since 2022/6/15 17:49
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object CommandColor {

    val command = subCommand {
        dynamic("color") {
            suggestion<Player> { sender, _ ->
                MessageColors.getColors(sender) + listOf("reset")
            }
            execute<Player> { sender, _, argument ->
                if (argument == "reset") {
                    sender.getSession().selectColor(null)
                } else {
                    sender.getSession().selectColor(argument)
                }

            }
        }
    }
}