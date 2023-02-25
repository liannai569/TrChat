package me.arasple.mc.trchat.api.impl

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.*
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory

/**
 * @author ItsFlicker
 * @since 2022/6/18 15:26
 */
@Awake
object DefaultTrChatAPI : TrChatAPI {

    init {
        TrChat.register(this)
    }

    override fun getComponentManager() = PlatformFactory.getAPI<ComponentManager>()

    override fun getProxyManager() = PlatformFactory.getAPI<ProxyManager>()

    override fun getChannelManager() = PlatformFactory.getAPI<ChannelManager>()

    override fun getFilterManager() = PlatformFactory.getAPI<FilterManager>()

}