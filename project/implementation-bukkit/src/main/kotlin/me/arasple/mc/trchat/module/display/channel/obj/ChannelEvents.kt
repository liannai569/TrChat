package me.arasple.mc.trchat.module.display.channel.obj

import me.arasple.mc.trchat.module.internal.script.Reaction
import org.bukkit.entity.Player

/**
 * @author wlys
 * @since 2022/6/15 18:03
 */
class ChannelEvents(
    private val process: Reaction?,
    private val send: Reaction?,
    private val join: Reaction?,
    private val quit: Reaction?
) {

    fun process(sender: Player, message: String): String? {
        process ?: return message
        return when (val result = process.eval(sender, "message" to message)) {
            null -> message
            is Boolean -> message.takeIf { result }
            is String -> result
            else -> message
        }
    }

    fun send(sender: Player, receiver: String, message: String): Boolean {
        send ?: return true
        return when (val result = send.eval(sender, "receiver" to receiver, "message" to message)) {
            null -> true
            is Boolean -> result
            else -> true
        }
    }

    fun join(sender: Player) {
        join?.eval(sender)
    }

    fun quit(sender: Player) {
        quit?.eval(sender)
    }

}