package me.arasple.mc.trchat.module.display.function

import me.arasple.mc.trchat.api.event.TrChatReloadEvent
import me.arasple.mc.trchat.module.internal.script.Reaction
import me.arasple.mc.trchat.util.legacy
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.entity.Player
import taboolib.common.io.getInstance
import taboolib.common.io.runningClassesWithoutLibrary
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.severe
import taboolib.common.util.VariableReader
import taboolib.common.util.replaceWithOrder
import taboolib.common5.mirrorNow
import taboolib.module.lang.*

abstract class Function(val id: String) {

    open val alias = id

    open val reaction: Reaction? = null

    abstract fun createVariable(sender: Player, message: String): String

    abstract fun parseVariable(sender: Player, forward: Boolean, arg: String): Component?

    abstract fun canUse(sender: Player): Boolean

    protected open fun <T> mirrorParse(func: () -> T): T {
        return mirrorNow("Function:$alias:ParseVariable", func)
    }

    companion object {

        @JvmStatic
        val functions = mutableListOf<Function>()

        private val parser = VariableReader("[", "]")

        fun reload(customFunctions: List<CustomFunction>) {
            functions.clear()
            functions.addAll(runningClassesWithoutLibrary
                .filter { it.isAnnotationPresent(StandardFunction::class.java) }
                .mapNotNull { it.getInstance()?.get() as? Function }
            )
            functions.addAll(customFunctions)
            TrChatReloadEvent.Function(functions).call()
        }

        fun Player.getComponentFromLang(node: String, vararg args: Any): TextComponent? {
            val sender = adaptPlayer(this)
            val file = sender.getLocaleFile() ?: return null
            return when (val type = file.nodes[node].let { if (it is TypeList) it.list[0] else it }) {
                is TypeJson -> {
                    var i = 0
                    val builder = Component.text()
                    parser.readToFlatten(type.text!!.joinToString()).forEach { part ->
                        builder.append(legacy(
                            part.text
                            .replace("\\[", "[").replace("\\]", "]")
                            .translate(sender).replaceWithOrder(*args)
                        ).toBuilder().run {
                            if (part.isVariable) {
                                val arg = type.jsonArgs.getOrNull(i++)
                                if (arg != null) {
                                    if (arg.containsKey("hover")) {
                                        hoverEvent(HoverEvent.showText(legacy(arg["hover"].toString().translate(sender).replaceWithOrder(*args))))
                                    }
                                    if (arg.containsKey("command")) {
                                        clickEvent(ClickEvent.runCommand(arg["command"].toString().translate(sender).replaceWithOrder(*args)))
                                    }
                                    if (arg.containsKey("suggest")) {
                                        clickEvent(ClickEvent.suggestCommand(arg["suggest"].toString().translate(sender).replaceWithOrder(*args)))
                                    }
                                    if (arg.containsKey("insertion")) {
                                        insertion(arg["insertion"].toString().translate(sender).replaceWithOrder(*args))
                                    }
                                    if (arg.containsKey("copy")) {
                                        clickEvent(ClickEvent.copyToClipboard(arg["copy"].toString().translate(sender).replaceWithOrder(*args)))
                                    }
                                    if (arg.containsKey("file")) {
                                        clickEvent(ClickEvent.openFile(arg["file"].toString().translate(sender).replaceWithOrder(*args)))
                                    }
                                    if (arg.containsKey("url")) {
                                        clickEvent(ClickEvent.openUrl(arg["url"].toString().translate(sender).replaceWithOrder(*args)))
                                    }
                                }
                            }
                            this
                        }.build())
                    }
                    builder.build()
                }
                is TypeText -> {
                    legacy(type.asText(adaptPlayer(this), *args)!!)
                }
                else -> {
                    severe("Error language type for functions (Required TypeJson or TypeText)")
                    null
                }
            }
        }

        private fun String.translate(sender: ProxyCommandSender): String {
            var s = this
            Language.textTransfer.forEach { s = it.translate(sender, s) }
            return s
        }

    }
}