package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.internal.data.Databases
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submitAsync

/**
 * @author ItsFlicker
 * @since 2021/12/11 23:19
 */
@PlatformSide([Platform.BUKKIT])
object ListenerQuit {

    private fun disconnect(player: Player) {
        Channel.channels.values.forEach { it.listeners -= player.name }

        ChatSession.removeSession(player)

        submitAsync {
            Databases.database.push(player)
            Databases.database.release(player)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onQuit(e: PlayerQuitEvent) {
        disconnect(e.player)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onKick(e: PlayerKickEvent) {
        disconnect(e.player)
    }
}