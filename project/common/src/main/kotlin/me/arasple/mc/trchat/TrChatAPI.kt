package me.arasple.mc.trchat

import taboolib.common.platform.ProxyCommandSender
import java.util.concurrent.CompletableFuture

/**
 * @author wlys
 * @since 2022/6/18 15:13
 */
interface TrChatAPI {

    fun getComponentManager(): ComponentManager

    fun getProxyManager(): ProxyManager

    fun getChannelManager(): ChannelManager

    fun getFilterManager(): FilterManager

    /**
     * 执行Kether脚本 (命名空间为trchat, trmenu, trhologram)
     */
    fun eval(sender: ProxyCommandSender, script: String, vararg vars: Pair<String, Any?>): CompletableFuture<Any?>

    /**
     * 执行Kether脚本 (命名空间为trchat, trmenu, trhologram)
     */
    fun eval(sender: ProxyCommandSender, script: List<String>, vararg vars: Pair<String, Any?>): CompletableFuture<Any?>

}