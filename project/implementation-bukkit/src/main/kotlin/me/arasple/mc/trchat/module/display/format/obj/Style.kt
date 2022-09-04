package me.arasple.mc.trchat.module.display.format.obj

import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.util.color.colorify
import me.arasple.mc.trchat.util.legacy
import me.arasple.mc.trchat.util.pass
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.util.replaceWithOrder
import taboolib.platform.compat.replacePlaceholder

sealed interface Style {

    val contents: List<Pair<String, Condition?>>

    fun process(builder: TextComponent.Builder, content: String)

    data class Font(override val contents: List<Pair<String, Condition?>>) : Style {
        override fun process(builder: TextComponent.Builder, content: String) {
            builder.font(content.split(":", limit = 2).let { Key.key(it[0], it[1]) })
        }
    }

    data class Insertion(override val contents: List<Pair<String, Condition?>>) : Style {
        override fun process(builder: TextComponent.Builder, content: String) {
            builder.insertion(content)
        }
    }

    sealed interface Hover : Style {

        data class Text(override val contents: List<Pair<String, Condition?>>) : Hover {
            override fun process(builder: TextComponent.Builder, content: String) {
                builder.hoverEvent(HoverEvent.showText(legacy(content)))
            }
        }

        data class Entity(override val contents: List<Pair<String, Condition?>>) : Hover {
            override fun process(builder: TextComponent.Builder, content: String) {
//                builder.hoverEvent(HoverEvent.showEntity())
            }
        }

    }

    sealed interface Click : Style {

        data class Suggest(override val contents: List<Pair<String, Condition?>>) : Style {
            override fun process(builder: TextComponent.Builder, content: String) {
                builder.clickEvent(ClickEvent.suggestCommand(content))
            }
        }

        data class Command(override val contents: List<Pair<String, Condition?>>) : Style {
            override fun process(builder: TextComponent.Builder, content: String) {
                builder.clickEvent(ClickEvent.runCommand(content))
            }
        }

        data class Url(override val contents: List<Pair<String, Condition?>>) : Style {
            override fun process(builder: TextComponent.Builder, content: String) {
                builder.clickEvent(ClickEvent.openUrl(content))
            }
        }

        data class Copy(override val contents: List<Pair<String, Condition?>>) : Style {
            override fun process(builder: TextComponent.Builder, content: String) {
                builder.clickEvent(ClickEvent.copyToClipboard(content))
            }
        }

        data class File(override val contents: List<Pair<String, Condition?>>) : Style {
            override fun process(builder: TextComponent.Builder, content: String) {
                builder.clickEvent(ClickEvent.openFile(content))
            }
        }

    }

    companion object {

        private fun String.setPlaceholders(sender: CommandSender) = if (sender is Player) {
            replacePlaceholder(sender)
        } else {
            this
        }

        fun Style.applyTo(builder: TextComponent.Builder, sender: CommandSender, vararg vars: String, message: String = "") {
            val content = when (this) {
                is Font -> {
                    contents.first { it.second.pass(sender) }.first
                }
                is Hover.Text -> {
                    contents.filter { it.second.pass(sender) }.joinToString("\n") { it.first }
                        .replace("\$message", message).replaceWithOrder(*vars).setPlaceholders(sender).colorify()
                }
                else -> {
                    contents.first { it.second.pass(sender) }.first
                        .replace("\$message", message).replaceWithOrder(*vars).setPlaceholders(sender)
                }
            }
            process(builder, content)
        }

    }
}
