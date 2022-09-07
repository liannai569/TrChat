package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.module.display.format.obj.Style
import me.arasple.mc.trchat.module.display.format.obj.Style.Companion.applyTo
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.util.color.CustomColor
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.legacy
import me.arasple.mc.trchat.util.pass
import me.arasple.mc.trchat.util.session
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.util.VariableReader
import taboolib.common5.mirrorNow

/**
 * @author wlys
 * @since 2021/12/12 13:46
 */
class MsgComponent(val defaultColor: List<Pair<CustomColor, Condition?>>, style: List<Style>) : JsonComponent(null, style) {

    fun createComponent(sender: CommandSender, msg: String, disabledFunctions: List<String>, forward: Boolean): TextComponent {
        val builder = Component.text()
        var message = msg

        if (sender !is Player) {
            return toTextComponent(sender, message)
        }

        Function.functions.filter { it.alias !in disabledFunctions && it.canUse(sender) }.forEach {
            message = it.createVariable(sender, message)
        }

        val defaultColor = sender.session.getColor(defaultColor.firstOrNull { it.second.pass(sender) }?.first)

        for (part in parser.readToFlatten(message)) {
            if (part.isVariable) {
                val args = part.text.split(":", limit = 2)
                val function = Function.functions.firstOrNull { it.id == args[0] }
                if (function != null) {
                    function.parseVariable(sender, forward, args[1])?.let { builder.append(it) }
                    function.reaction?.eval(sender, "message" to message)
                }
                continue
            }
            builder.append(toTextComponent(sender, MessageColors.defaultColored(defaultColor, sender, part.text)))
        }
        return builder.build()
    }

    override fun toTextComponent(sender: CommandSender, vararg vars: String): TextComponent {
        return mirrorNow("Chat:Format:Msg") {
            val message = vars[0]
            val builder = legacy(message).toBuilder()
            val originMessage = builder.content()

            style.forEach {
                it.applyTo(builder, sender, *vars, message = originMessage)
            }

            builder.build()
        }
    }

    companion object {

        private val parser = VariableReader()

    }
}