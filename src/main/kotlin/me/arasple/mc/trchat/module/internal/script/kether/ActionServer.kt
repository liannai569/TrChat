package me.arasple.mc.trchat.module.internal.script.kether

import me.arasple.mc.trchat.util.proxy.bungee.Bungees
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author wlys
 * @since 2021/8/29 15:44
 */
class ActionServer(val server: ParsedAction<*>): ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        val s = frame.script()
        if (s.sender == null) {
            error("No sender selected.")
        }
        frame.newFrame(server).run<Any>().thenAccept { server ->
            Bungees.sendBungeeData(s.sender!!.cast(), "Connect", server.toString())
        }
        return CompletableFuture.completedFuture(null)
    }

    @Internal
    @PlatformSide([Platform.BUKKIT])
    companion object {

        @KetherParser(["server", "bungee", "connect"], shared = true)
        fun parser() = scriptParser {
            ActionServer(it.next(ArgTypes.ACTION))
        }
    }
}