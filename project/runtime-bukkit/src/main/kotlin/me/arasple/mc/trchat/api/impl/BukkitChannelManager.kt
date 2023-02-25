package me.arasple.mc.trchat.api.impl

import me.arasple.mc.trchat.api.ChannelManager
import me.arasple.mc.trchat.module.conf.Loader
import me.arasple.mc.trchat.module.display.channel.Channel
import taboolib.common.platform.*

/**
 * @author ItsFlicker
 * @since 2022/6/19 19:57
 */
@Awake
@PlatformSide([Platform.BUKKIT])
object BukkitChannelManager : ChannelManager {

    init {
        PlatformFactory.registerAPI<ChannelManager>(this)
    }

    override fun loadChannels(sender: ProxyCommandSender) {
        Loader.loadChannels(sender)
    }

    override fun getChannel(id: String): Channel? {
        return Channel.channels[id]
    }

}