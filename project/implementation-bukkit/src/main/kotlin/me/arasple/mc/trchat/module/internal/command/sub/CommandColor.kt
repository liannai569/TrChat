package me.arasple.mc.trchat.module.internal.command.sub

import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.data
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.subCommand

/**
 * @author wlys
 * @since 2022/6/15 17:49
 */
@PlatformSide([Platform.BUKKIT])
object CommandColor {

    val command = subCommand {
        dynamic("color") {
            suggestion<Player>(uncheck = true) { sender, _ ->
                MessageColors.getColors(sender) + "reset"
            }
            restrict<Player> { sender, _, argument ->
                argument.equals("reset", ignoreCase = true) || sender.hasPermission(MessageColors.COLOR_PERMISSION_NODE + argument)
            }
            execute<Player> { sender, _, argument ->
                if (argument == "reset") {
                    sender.data.selectColor(null)
                } else {
                    sender.data.selectColor(argument)
                }

            }
        }
    }
}