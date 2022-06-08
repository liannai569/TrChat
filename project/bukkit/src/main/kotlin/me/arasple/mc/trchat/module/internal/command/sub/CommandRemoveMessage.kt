package me.arasple.mc.trchat.module.internal.command.sub

import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.getSession
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.onlinePlayers

/**
 * CommandIgnore
 * me.arasple.mc.trchat.module.internal.command
 *
 * @author wlys
 * @since 2021/8/11 12:08
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object CommandRemoveMessage {

    val command = subCommand {
        dynamic("message") {
            suggestion<Player> { sender, _ ->
                sender.getSession().receivedMessages.mapNotNull { it.message }
            }
            execute<Player> { _, _, argument ->
                onlinePlayers().forEach { it.cast<Player>().getSession().removeMessage(argument) }
                onlinePlayers().forEach { it.cast<Player>().getSession().releaseMessage() }
            }
        }
    }
}