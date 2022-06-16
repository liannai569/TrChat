package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.color.MessageColors
import org.bukkit.event.player.PlayerEditBookEvent
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
object ListenerBookEdit {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBookEdit(e: PlayerEditBookEvent) {
        val p = e.player
        if (Settings.CONF.getBoolean("Color.Book", true)) {
            val meta = e.newBookMeta
            meta.pages = MessageColors.replaceWithPermission(p, meta.pages, MessageColors.Type.BOOK)
            e.newBookMeta = meta
        }
    }
}