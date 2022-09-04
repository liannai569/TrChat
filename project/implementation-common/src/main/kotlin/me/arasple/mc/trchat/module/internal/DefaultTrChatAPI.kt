package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.*
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common5.mirrorNow
import taboolib.library.kether.LocalizedException
import taboolib.module.kether.KetherShell
import java.util.concurrent.CompletableFuture

/**
 * @author wlys
 * @since 2022/6/18 15:26
 */
@Awake
object DefaultTrChatAPI : TrChatAPI {

    init {
        TrChat.register(this)
    }

    override fun getComponentManager(): ComponentManager {
        return PlatformFactory.getAPI()
    }

    override fun getProxyManager(): ProxyManager {
        return PlatformFactory.getAPI()
    }

    override fun getChannelManager(): ChannelManager {
        return PlatformFactory.getAPI()
    }

    override fun getFilterManager(): FilterManager {
        return PlatformFactory.getAPI()
    }

    override fun eval(sender: ProxyCommandSender, script: String, vararg vars: Pair<String, Any?>): CompletableFuture<Any?> {
        return mirrorNow("Handler:Script:Evaluation") {
            return@mirrorNow try {
                KetherShell.eval(
                    script,
                    namespace = listOf("trchat", "trmenu", "trhologram"),
                    sender = sender,
                    vars = KetherShell.VariableMap(vars.toMap())
                )
            } catch (e: LocalizedException) {
                println("§c[TrChat] §8Unexpected exception while parsing kether shell:")
                e.localizedMessage.split("\n").forEach {
                    println("         §8$it")
                }
                CompletableFuture.completedFuture(null)
            }
        }
    }

    override fun eval(sender: ProxyCommandSender, script: List<String>, vararg vars: Pair<String, Any?>): CompletableFuture<Any?> {
        return eval(sender, script.joinToString("\n"), *vars)
    }
}