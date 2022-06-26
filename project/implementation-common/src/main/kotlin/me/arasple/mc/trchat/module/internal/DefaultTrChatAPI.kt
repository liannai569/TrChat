package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.*
import me.arasple.mc.trchat.module.display.filter.processer.Filter
import me.arasple.mc.trchat.module.display.filter.processer.FilteredObject
import me.arasple.mc.trchat.module.internal.service.Metrics
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
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

    override fun filter(string: String, execute: Boolean): FilteredObject {
        return mirrorNow("Handler:DoFilter") {
            Filter.doFilter(string, execute).also {
                Metrics.increase(1, it.sensitiveWords)
            }
        }
    }

    override fun filterString(player: ProxyPlayer, string: String, execute: Boolean): FilteredObject {
        return if (execute) {
            filter(string, !player.hasPermission("trchat.bypass.filter"))
        } else {
            FilteredObject(string, 0)
        }
    }

    override fun eval(sender: ProxyCommandSender, script: String, vararg vars: Pair<String, Any?>): CompletableFuture<Any?> {
        return mirrorNow("Handler:Script:Evaluation") {
            return@mirrorNow try {
                KetherShell.eval(script, namespace = listOf("trchat", "trmenu", "trhologram"), sender = sender) {
                    vars.forEach {
                        set(it.first, it.second)
                    }
                }
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