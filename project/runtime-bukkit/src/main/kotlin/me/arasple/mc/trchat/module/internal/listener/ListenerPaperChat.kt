package me.arasple.mc.trchat.module.internal.listener

import io.papermc.paper.event.player.AsyncChatEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

@PlatformSide([Platform.BUKKIT])
object ListenerPaperChat {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPaperChat(e: AsyncChatEvent) {
        e.isCancelled = true
    }

}