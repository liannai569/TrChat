package me.arasple.mc.trchat.module.display.format.part

import me.arasple.mc.trchat.module.internal.script.Condition
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender

/**
 * @author wlys
 * @since 2022/1/21 23:22
 */
abstract class Part {

    abstract val content: String

    abstract val condition: Condition?

    abstract val dynamic: Boolean

    abstract fun process(builder: TextComponent.Builder, sender: CommandSender, vararg vars: String, message: String = "")
}