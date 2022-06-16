package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.internal.data.Database
import me.arasple.mc.trchat.util.Internal
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

/**
 * @author wlys
 * @since 2021/12/11 23:19
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object ListenerQuit {

    @SubscribeEvent(EventPriority.HIGHEST)
    fun e(e: PlayerQuitEvent) {
        val player = e.player

        Channel.channels.forEach { it.listeners.remove(player.uniqueId) }

        ChatSession.removeSession(player)

        submit(async = true) {
            Database.database.push(player)
            Database.database.release(player)
        }
    }

    @SubscribeEvent(EventPriority.HIGHEST, ignoreCancelled = true)
    fun e(e: PlayerKickEvent) {
        val player = e.player

        Channel.channels.forEach { it.listeners.remove(player.uniqueId) }

        ChatSession.removeSession(player)

        submit(async = true) {
            Database.database.push(player)
            Database.database.release(player)
        }
    }
}