package me.arasple.mc.trchat

import taboolib.common.platform.ProxyCommandSender

/**
 * @author ItsFlicker
 * @since 2022/6/19 19:55
 */
interface ChannelManager {

    fun loadChannels(sender: ProxyCommandSender)

    fun getChannel(id: String): Any?

}