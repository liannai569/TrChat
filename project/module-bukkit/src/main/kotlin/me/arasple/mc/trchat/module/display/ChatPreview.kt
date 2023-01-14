package me.arasple.mc.trchat.module.display

import me.arasple.mc.trchat.api.nms.NMS
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.module.nms.PacketReceiveEvent

/**
 * @author ItsFlicker
 * @since 2022/6/9 21:24
 */
@PlatformSide([Platform.BUKKIT])
object ChatPreview {

    @SubscribeEvent
    fun onReceive(e: PacketReceiveEvent) {
        if (e.packet.name == "ServerboundChatPreviewPacket") {
            val queryId = e.packet.source.invokeMethod<Int>("queryId")!!
            val query = e.packet.source.invokeMethod<String>("query")!!
            NMS.INSTANCE.sendChatPreview(e.player, queryId, query)
        }
    }

}