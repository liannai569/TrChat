package me.arasple.mc.trchat.module.display.format.part.json

import me.arasple.mc.trchat.module.display.format.part.Part
import me.arasple.mc.trchat.module.internal.script.Condition
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender

/**
 * @author wlys
 * @since 2022/4/9 12:05
 */
class Font(override val content: String, override val condition: Condition?) : Part() {

    override val dynamic = false

    private val key = content.split(":").let { Key.key(it[0], it[1]) }

    override fun process(builder: TextComponent.Builder, sender: CommandSender, vararg vars: String, message: String) {
        builder.font(key)
    }
}