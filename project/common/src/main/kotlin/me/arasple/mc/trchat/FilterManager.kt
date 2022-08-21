package me.arasple.mc.trchat

import me.arasple.mc.trchat.module.internal.filter.processer.FilteredObject
import taboolib.common.platform.ProxyPlayer

interface FilterManager {

    /**
     * 根据玩家的权限 (trchat.bypass.filter)，过滤一个字符串
     *
     * @param string  待过滤字符串
     * @param execute 是否真的过滤
     * @param player 玩家
     * @return 过滤后的字符串
     */
    fun filter(string: String, execute: Boolean = true, player: ProxyPlayer? = null): FilteredObject

}