package me.arasple.mc.trchat.module.internal.script.kether

import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyManager
import me.arasple.mc.trchat.util.Internal
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author wlys
 * @since 2021/8/29 15:44
 */
class ActionServer(val server: String): ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        val s = frame.script()
        if (s.sender == null) {
            error("No sender selected.")
        }

        BukkitProxyManager.sendCommonMessage(s.sender!!.cast(), "Connect", server)

        return CompletableFuture.completedFuture(null)
    }

    @Internal
    @PlatformSide([Platform.BUKKIT])
    companion object {

        @KetherParser(["server", "bungee", "connect"], shared = true)
        fun parser() = scriptParser {
            ActionServer(it.nextToken())
        }
    }
}