package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.module.display.format.obj.Style
import me.arasple.mc.trchat.module.display.format.obj.Style.Companion.applyTo
import me.arasple.mc.trchat.module.display.format.obj.Text
import me.arasple.mc.trchat.util.pass
import org.bukkit.command.CommandSender
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
open class JsonComponent(
    val text: List<Text>?,
    val style: List<Style>
) {

    open fun toTextComponent(sender: CommandSender, vararg vars: String): ComponentText {
        val component = text?.firstOrNull { it.condition.pass(sender) }?.process(sender, *vars) ?: Components.empty()
        style.forEach {
            it.applyTo(component, sender, *vars)
        }
        return component
    }
}