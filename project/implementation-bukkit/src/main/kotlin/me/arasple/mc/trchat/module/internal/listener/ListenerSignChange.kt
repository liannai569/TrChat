package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.api.config.Filters
import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.color.MessageColors
import org.bukkit.event.block.SignChangeEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

/**
 * @author Arasple, wlys
 * @date 2019/8/15 21:18
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object ListenerSignChange {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onSignChange(e: SignChangeEvent) {
        val p = e.player

        e.lines.forEachIndexed { index, l ->
            e.setLine(index, l.let {
                var line = it
                if (Settings.CONF.getBoolean("Color.Sign")) {
                    line = MessageColors.replaceWithPermission(p, line ?: "", MessageColors.Type.SIGN)
                }
                if (Filters.CONF.getBoolean("Enable.Sign")) {
                    TrChatAPI.filterString(p, line).filtered
                } else {
                    line
                }
            })
        }
    }
}