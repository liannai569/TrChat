package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.display.format.part.json.*
import me.arasple.mc.trchat.module.display.function.*
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
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.VariableReader
import taboolib.common5.mirrorNow

/**
 * @author wlys
 * @since 2021/12/12 13:46
 */
class MsgComponent(
    val defaultColor: List<Pair<CustomColor, Condition?>>,
    hover: Hover?,
    suggest: List<Suggest>?,
    command: List<Command>?,
    url: List<Url>?,
    insertion: List<Insertion>?,
    copy: List<Copy>?,
    font: List<Font>?
) : JsonComponent(null, hover, suggest, command, url, insertion, copy, font) {

    fun serialize(sender: CommandSender, msg: String, disabledFunctions: List<String>): TextComponent {
        val component = Component.text()
        var message = msg

        if (sender !is Player) {
            return toTextComponent(sender, message)
        }

        if (!disabledFunctions.contains("Item-Show") && sender.passPermission(ItemShow.permission)) {
            message = ItemShow.replaceMessage(message, sender)
        }
        if (!disabledFunctions.contains("Mention") && sender.passPermission(Mention.permission)) {
            message = Mention.replaceMessage(message, sender)
        }
        if (!disabledFunctions.contains("Inventory-Show") && sender.passPermission(InventoryShow.permission)) {
            message = InventoryShow.replaceMessage(message)
        }
        if (!disabledFunctions.contains("EnderChest-Show") && sender.passPermission(EnderChestShow.permission)) {
            message = EnderChestShow.replaceMessage(message)
        }
        Function.functions.filter { it.condition.pass(sender) && !disabledFunctions.contains(it.id) }.forEach {
            message = it.apply(message)
        }

        val defaultColor = sender.session.getColor(defaultColor.first { it.second.pass(sender) }.first)

        for (part in parser.readToFlatten(message)) {
            if (part.isVariable) {
                val args = part.text.split(":", limit = 2)
                when (val id = args[0]) {
                    "ITEM" -> {
                        component.append(ItemShow.createComponent(sender, args[1].toInt()))
                        continue
                    }
                    "MENTION" -> {
                        component.append(Mention.createComponent(sender, args[1]))
                        continue
                    }
                    "INVENTORY" -> {
                        component.append(InventoryShow.createComponent(sender))
                        continue
                    }
                    "ENDERCHEST" -> {
                        component.append(EnderChestShow.createComponent(sender))
                        continue
                    }
                    else -> {
                        val function = Function.functions.firstOrNull { it.id == id }
                        if (function != null) {
                            component.append(function.displayJson.toTextComponent(sender, args[1]))
                            function.action?.let { action -> TrChat.api().eval(adaptPlayer(sender), action) }
                            continue
                        }
                    }
                }
            }
            component.append(toTextComponent(sender, MessageColors.defaultColored(defaultColor, sender, part.text)))
        }
        return component.build()
    }

    override fun toTextComponent(sender: CommandSender, vararg vars: String): TextComponent {
        return mirrorNow("Chat:Format:Msg") {
            val message = vars[0]
            val builder = legacy(message).toBuilder()
            val originMessage = builder.content()

            hover?.process(builder, sender, *vars, message = originMessage)
            suggest?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars, message = originMessage)
            command?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars, message = originMessage)
            url?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars, message = originMessage)
            insertion?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars, message = originMessage)
            copy?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars, message = originMessage)
            font?.firstOrNull { it.condition.pass(sender) }?.process(builder, sender, *vars, message = originMessage)

            builder.build()
        }
    }

    companion object {

        private val parser = VariableReader()

        private fun Player.passPermission(permission: String?): Boolean {
            return permission == null || permission == "null" || hasPermission(permission)
        }

    }
}