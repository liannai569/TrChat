package me.arasple.mc.trchat

import me.arasple.mc.trchat.module.display.filter.processer.FilteredObject
import taboolib.common.platform.ProxyCommandSender

/**
 * @author wlys
 * @since 2022/6/18 15:13
 */
interface TrChatAPI {

    fun getComponentManager(): ComponentManager

    fun getProxyManager(): ProxyManager

    /**
     * 根据玩家的权限 (trchat.bypass.filter)，过滤字符串
     *
     * @param player 玩家
     * @param string 字符串
     * @param execute 是否真的过滤
     * @return 过滤后的
     */
    fun filterString(player: ProxyCommandSender, string: String, execute: Boolean = true): FilteredObject

    /**
     * 过滤一个字符串
     *
     * @param string  待过滤字符串
     * @param execute 是否真的过滤
     * @return 过滤后的字符串
     */
    fun filter(string: String, execute: Boolean = true): FilteredObject

}