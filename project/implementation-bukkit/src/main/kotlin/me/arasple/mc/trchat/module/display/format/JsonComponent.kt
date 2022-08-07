package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.module.display.format.obj.Style
import me.arasple.mc.trchat.module.display.format.obj.Style.Companion.applyTo
import me.arasple.mc.trchat.module.display.format.obj.Text
import me.arasple.mc.trchat.util.pass
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender
import taboolib.common5.mirrorNow

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
open class JsonComponent(
    val text: List<Text>?,
    val style: List<Style>
) {

    open fun toTextComponent(sender: CommandSender, vararg vars: String): TextComponent {
        return mirrorNow("Chat:Format:Json") {
            val builder = text?.firstOrNull { it.condition.pass(sender) }?.process(sender, *vars) ?: Component.text()
            style.forEach {
                it.applyTo(builder, sender, *vars)
            }
            builder.build()
        }
    }
}