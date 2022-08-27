package me.arasple.mc.trchat.module.display.function

import me.arasple.mc.trchat.module.display.format.JsonComponent
import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.module.internal.script.Reaction
import me.arasple.mc.trchat.util.pass
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * @author wlys
 * @since 2021/12/12 11:41
 */
class CustomFunction(
    id: String,
    val condition: Condition?,
    val priority: Int,
    val regex: Regex,
    val filterTextRegex: Regex?,
    val displayJson: JsonComponent,
    override val reaction: Reaction?
) : Function(id) {

    override fun createVariable(sender: Player, message: String): String {
        return message.replaceRegex(regex, filterTextRegex) { "{{$id:$it}}" }
    }

    override fun parseVariable(sender: Player, forward: Boolean, arg: String): Component {
        return displayJson.toTextComponent(sender, arg)
    }

    override fun canUse(sender: Player): Boolean {
        return condition.pass(sender)
    }

    companion object {

        fun String.replaceRegex(regex: Regex, replaceRegex: Regex?, replacement: (String) -> String): String {
            var string = this
            regex.findAll(string).forEach {
                val str = it.value
                val result = replaceRegex?.find(str)?.value ?: str
                val rep = replacement(result)
                string = string.replaceFirst(str, rep)
            }
            return string
        }

    }
}