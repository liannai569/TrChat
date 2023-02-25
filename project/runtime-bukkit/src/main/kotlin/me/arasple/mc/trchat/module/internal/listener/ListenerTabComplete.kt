package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.module.conf.file.Settings
import org.bukkit.event.player.PlayerCommandSendEvent
import taboolib.common.platform.Ghost
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent

/**
 * ListenerTabComplete
 * me.arasple.mc.trchat.internal.listener
 *
 * @author ItsFlicker
 * @since 2021/10/22 23:25
 */
@PlatformSide([Platform.BUKKIT])
object ListenerTabComplete {

    @Ghost
    @SubscribeEvent
    fun onTab(e: PlayerCommandSendEvent) {
        if (Settings.conf.getBoolean("Options.Prevent-Tab-Complete", false)
            && !e.player.hasPermission("trchat.bypass.tabcomplete")) {
            e.commands.clear()
        }
    }
}