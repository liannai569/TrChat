package me.arasple.mc.trchat.module.display.format.obj

import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.util.Regexs
import me.arasple.mc.trchat.util.color.colorify
import me.arasple.mc.trchat.util.legacy
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.util.replaceWithOrder
import taboolib.common.util.unsafeLazy
import taboolib.module.kether.KetherTransfer
import taboolib.platform.compat.replacePlaceholder

/**
 * @author wlys
 * @since 2022/1/21 23:21
 */
class Text(val content: String, val condition: Condition?) {

    val dynamic by unsafeLazy { Regexs.containsPlaceholder(content) }

    fun process(sender: CommandSender, vararg vars: String): TextComponent.Builder {
        var text = KetherTransfer.translate(adaptCommandSender(sender), content).replaceWithOrder(*vars)
        if (dynamic && sender is Player) {
            text = text.replacePlaceholder(sender)
        }
        return legacy(text.colorify()).toBuilder()
    }
}