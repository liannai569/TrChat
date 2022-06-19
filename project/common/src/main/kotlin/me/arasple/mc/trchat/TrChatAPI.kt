package me.arasple.mc.trchat

import me.arasple.mc.trchat.module.display.filter.processer.FilteredObject
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import java.util.concurrent.CompletableFuture

/**
 * @author wlys
 * @since 2022/6/18 15:13
 */
interface TrChatAPI {

    fun getComponentManager(): ComponentManager

    fun getProxyManager(): ProxyManager

    fun getChannelManager(): ChannelManager

    /**
     * 过滤一个字符串
     *
     * @param string  待过滤字符串
     * @param execute 是否真的过滤
     * @return 过滤后的字符串
     */
    fun filter(string: String, execute: Boolean = true): FilteredObject

    /**
     * 根据玩家的权限 (trchat.bypass.filter)，过滤字符串
     *
     * @param player 玩家
     * @param string 字符串
     * @param execute 是否真的过滤
     * @return 过滤后的
     */
    fun filterString(player: ProxyPlayer, string: String, execute: Boolean = true): FilteredObject

    /**
     * 执行Kether脚本 (命名空间为trchat, trmenu, trhologram)
     */
    fun eval(sender: ProxyCommandSender, script: String): CompletableFuture<Any?>

    /**
     * 执行Kether脚本 (命名空间为trchat, trmenu, trhologram)
     */
    fun eval(sender: ProxyCommandSender, script: List<String>): CompletableFuture<Any?>

}