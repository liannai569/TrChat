package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.module.display.format.part.json.*
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
    val hover: Hover?,
    val suggest: List<Suggest>?,
    val command: List<Command>?,
    val url: List<Url>?,
    val insertion: List<Insertion>?,
    val copy: List<Copy>?,
    val font: List<Font>?
) {

    open fun toTextComponent(sender: CommandSender, vararg vars: String): TextComponent {
        return mirrorNow("Chat:Format:Json") {
            val builder = text?.firstOrNull { it.condition.pass(sender) }?.process(sender, *vars) ?: Component.text()

            hover?.process(builder, sender, *vars)
            suggest?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars)
            command?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars)
            url?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars)
            insertion?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars)
            copy?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars)
            font?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars)

            builder.build()
        }
    }
}