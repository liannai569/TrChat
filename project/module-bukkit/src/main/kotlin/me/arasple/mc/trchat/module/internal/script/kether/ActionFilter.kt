package me.arasple.mc.trchat.module.internal.script.kether

import me.arasple.mc.trchat.TrChat
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author ItsFlicker
 * @since 2021/8/29 15:44
 */
class ActionFilter {

    class ActionCheck(private val action: ParsedAction<*>): ScriptAction<Boolean>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
            var string = ""
            frame.newFrame(action).run<String>().thenAccept {
                string = it
            }
            return CompletableFuture.completedFuture(TrChat.api().getFilterManager().filter(string).sensitiveWords > 0)
        }

    }

    class ActionGet(private val action: ParsedAction<*>): ScriptAction<String>() {

        override fun run(frame: ScriptFrame): CompletableFuture<String> {
            var string = ""
            frame.newFrame(action).run<String>().thenAccept {
                string = it
            }
            return CompletableFuture.completedFuture(TrChat.api().getFilterManager().filter(string).filtered)
        }

    }

    @PlatformSide([Platform.BUKKIT])
    companion object {

        @KetherParser(["filter"], shared = true)
        fun parser() = scriptParser {
            it.switch {
                case("has", "have", "check") {
                    ActionCheck(it.next(ArgTypes.ACTION))
                }
                case("get") {
                    ActionGet(it.next(ArgTypes.ACTION))
                }
            }
        }
    }
}