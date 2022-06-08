package me.arasple.mc.trchat.module.internal.script.kether

import me.arasple.mc.trchat.module.display.channel.Channel
import taboolib.library.kether.LocalizedException
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author wlys
 * @since 2021/8/29 15:44
 */
class ActionChannel(val symbol: Symbol, val channel: String?): ScriptAction<Void>() {

    enum class Symbol {

        JOIN, QUIT
    }

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        val s = frame.script()
        if (s.sender == null) {
            error("No sender selected.")
        }
        when (symbol) {
            Symbol.JOIN -> Channel.join(s.sender!!.cast(), channel!!)
            Symbol.QUIT -> Channel.quit(s.sender!!.cast())
        }
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        @KetherParser(["channel"], namespace = "trchat", shared = true)
        fun parser() = scriptParser {
            val symbol = when(val type = it.nextToken().lowercase()) {
                "join" -> Symbol.JOIN
                "quit" -> Symbol.QUIT
                "leave" -> Symbol.QUIT
                else -> throw LocalizedException.of("load-error.custom", "Unknown channel operator $type")
            }
            ActionChannel(symbol, if (symbol == Symbol.JOIN) it.nextToken() else null)
        }
    }
}