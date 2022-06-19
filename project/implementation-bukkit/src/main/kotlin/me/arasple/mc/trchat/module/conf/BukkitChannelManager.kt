package me.arasple.mc.trchat.module.conf

import me.arasple.mc.trchat.ChannelManager
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.util.Internal
import taboolib.common.platform.*

/**
 * @author wlys
 * @since 2022/6/19 19:57
 */
@Awake
@Internal
@PlatformSide([Platform.BUKKIT])
object BukkitChannelManager : ChannelManager {

    init {
        PlatformFactory.registerAPI<ChannelManager>(this)
    }

    var loadedProxyChannels = false

    override fun loadChannels(sender: ProxyCommandSender) {
        Loader.loadChannels(sender)
    }

    override fun getChannel(id: String): Channel? {
        return Channel.channels[id]
    }

}