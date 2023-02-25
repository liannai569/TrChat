package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.impl.BukkitProxyManager
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.internal.service.Updater
import me.arasple.mc.trchat.util.data
import me.arasple.mc.trchat.util.passPermission
import me.arasple.mc.trchat.util.session
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.sendLang

/**
 * @author ItsFlicker
 * @since 2021/12/11 23:19
 */
@PlatformSide([Platform.BUKKIT])
object ListenerJoin {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player

        BukkitProxyManager.sendTrChatMessage(player, "FetchProxyChannels")

        Channel.channels.values.filter { it.settings.autoJoin }.forEach {
            if (player.passPermission(it.settings.joinPermission)) {
                it.listeners.add(player.name)
            }
        }
        player.session
        player.data

        submit(delay = 20) {
            if (player.isOnline && player.hasPermission("trchat.admin") && Updater.latest_Version > Updater.current_version && !Updater.notified.contains(player.uniqueId)) {
                player.sendLang("Plugin-Updater-Header", Updater.current_version, Updater.latest_Version)
                player.sendMessage(Updater.information)
                player.sendLang("Plugin-Updater-Footer")
                Updater.notified.add(player.uniqueId)
            }
        }
    }
}