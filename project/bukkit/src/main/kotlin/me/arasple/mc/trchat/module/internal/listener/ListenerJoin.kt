package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.getSession
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent

/**
 * @author wlys
 * @since 2021/12/11 23:19
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object ListenerJoin {

    @SubscribeEvent
    fun e(e: PlayerJoinEvent) {
        val player = e.player
        Channel.channels.filter { it.settings.autoJoin }.forEach {
            if (it.settings.joinPermission == null || player.hasPermission(it.settings.joinPermission)) {
                it.listeners.add(player.uniqueId)
            }
        }
        player.getSession()
    }
}