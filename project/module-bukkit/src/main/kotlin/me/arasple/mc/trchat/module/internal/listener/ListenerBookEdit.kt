package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.legacyColorToTag
import me.arasple.mc.trchat.util.miniMessage
import org.bukkit.event.player.PlayerEditBookEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

/**
 * @author ItsFlicker
 * @date 2019/8/15 21:18
 */
@PlatformSide([Platform.BUKKIT])
object ListenerBookEdit {

    @Suppress("Deprecation")
    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBookEdit(e: PlayerEditBookEvent) {
        val p = e.player
        val meta = e.newBookMeta
        if (Settings.CONF.getBoolean("Color.Book")) {
            meta.pages = MessageColors.replaceWithPermission(p, meta.pages, MessageColors.Type.BOOK)
        }
        if (TrChatBukkit.isPaperEnv && p.hasPermission("trchat.color.book.minimessage")) {
            meta.pages.forEachIndexed { index, string ->
                meta.page(index + 1, miniMessage.deserialize(string.legacyColorToTag()))
            }
        }
        e.newBookMeta = meta
    }
}