package me.arasple.mc.trchat.module.display.function

import me.arasple.mc.trchat.api.event.TrChatReloadEvent
import me.arasple.mc.trchat.module.internal.script.Reaction
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import taboolib.common.io.getInstance
import taboolib.common.io.runningClassesWithoutLibrary

abstract class Function(val id: String) {

    open val alias = id

    open val reaction: Reaction? = null

    abstract fun createVariable(sender: Player, message: String): String

    abstract fun parseVariable(sender: Player, forward: Boolean, arg: String): Component

    abstract fun canUse(sender: Player): Boolean

    companion object {

        @JvmStatic
        val functions = mutableListOf<Function>()

        fun reload(customFunctions: List<CustomFunction>) {
            functions.clear()
            if (TrChatReloadEvent.Function(customFunctions).call()) {
                functions.addAll(runningClassesWithoutLibrary
                    .filter { it.isAnnotationPresent(StandardFunction::class.java) }
                    .mapNotNull { it.getInstance()?.get() as? Function }
                )
                functions.addAll(customFunctions)
            }
        }

    }
}