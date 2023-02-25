package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.adventure.toAdventure
import me.arasple.mc.trchat.module.conf.file.Filters
import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.parseSimple
import org.bukkit.event.block.SignChangeEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer

/**
 * @author ItsFlicker
 * @date 2019/8/15 21:18
 */
@PlatformSide([Platform.BUKKIT])
object ListenerSignChange {

    @Suppress("Deprecation")
    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onSignChange(e: SignChangeEvent) {
        val p = e.player

        for (index in e.lines.indices) {
            if (Filters.conf.getBoolean("Enable.Sign")) {
                e.setLine(index, TrChat.api().getFilterManager().filter(e.getLine(index) ?: "", player = adaptPlayer(p)).filtered)
            }
            if (Settings.conf.getBoolean("Color.Sign")) {
                if (TrChatBukkit.isPaperEnv && p.hasPermission("trchat.color.simple")) {
                    e.line(index, (e.getLine(index) ?: "").parseSimple().toAdventure())
                } else {
                    e.setLine(index, MessageColors.replaceWithPermission(p, e.getLine(index) ?: "", MessageColors.Type.SIGN))
                }
            }
        }
    }
}