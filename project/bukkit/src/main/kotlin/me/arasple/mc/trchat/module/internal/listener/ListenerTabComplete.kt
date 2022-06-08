package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.util.Internal
import org.bukkit.event.player.PlayerCommandSendEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent

/**
 * ListenerTabComplete
 * me.arasple.mc.trchat.internal.listener
 *
 * @author wlys
 * @since 2021/10/22 23:25
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object ListenerTabComplete {

    @SubscribeEvent
    fun e(e: PlayerCommandSendEvent) {
        if (Settings.CONF.getBoolean("Options.Prevent-Tab-Complete", false)
            && !e.player.hasPermission("trchat.bypass.tabcomplete")) {
            e.commands.clear()
        } else {
            e.commands.addAll(Channel.channels.mapNotNull { it.bindings.command }.flatten())
        }
    }
}