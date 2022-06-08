package me.arasple.mc.trchat.module.display.format.part.json

import me.arasple.mc.trchat.module.display.format.part.Part
import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.util.Regexs
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.util.replaceWithOrder
import taboolib.platform.compat.replacePlaceholder

/**
 * @author wlys
 * @since 2022/1/21 23:21
 */
class Command(override val content: String, override val condition: Condition?) : Part() {

    override val dynamic by lazy { Regexs.containsPlaceholder(content) }

    override fun process(builder: TextComponent.Builder, sender: CommandSender, vararg vars: String, message: String) {
        if (!dynamic || sender !is Player) {
            builder.clickEvent(ClickEvent.runCommand(content.replace("\$message", message).replaceWithOrder(*vars)))
        } else {
            builder.clickEvent(ClickEvent.runCommand(content.replacePlaceholder(sender).replace("\$message", message).replaceWithOrder(*vars)))
        }
    }
}