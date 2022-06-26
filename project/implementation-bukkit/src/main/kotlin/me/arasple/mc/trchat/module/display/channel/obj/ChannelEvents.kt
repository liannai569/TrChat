package me.arasple.mc.trchat.module.display.channel.obj

import me.arasple.mc.trchat.module.internal.script.Reaction
import org.bukkit.entity.Player

/**
 * @author wlys
 * @since 2022/6/15 18:03
 */
class ChannelEvents(
    private val process: Reaction?,
    private val send: Reaction?
) {

    fun process(sender: Player, message: String): String? {
        process ?: return message
        return when (val result = process.eval(sender, "message" to message)) {
            null -> message
            is Boolean -> if (result) message else null
            is String -> result
            else -> message
        }
    }

    fun send(sender: Player, message: String) {
        send?.eval(sender, "message" to message)
    }
}