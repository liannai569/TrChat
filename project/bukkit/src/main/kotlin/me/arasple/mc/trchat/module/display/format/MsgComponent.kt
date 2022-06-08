package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.module.display.format.part.json.*
import me.arasple.mc.trchat.module.display.function.*
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.display.function.Function.Companion.replaceRegex
import me.arasple.mc.trchat.util.color.DefaultColor
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.legacy
import me.arasple.mc.trchat.util.pass
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
class MsgComponent(
    val defaultColor: DefaultColor,
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
            message = message.replaceRegex(it.regex, it.filterTextPattern, "{{${it.id}:{0}}}")
        }

        val defaultColor = MessageColors.catchDefaultMessageColor(sender, defaultColor)

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
                            function.action?.let { action -> TrChatAPI.eval(sender, action) }
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

    companion object {

        private val parser = VariableReader()

        private fun Player.passPermission(permission: String?): Boolean {
            return permission == null || permission == "null" || hasPermission(permission)
        }

    }
}