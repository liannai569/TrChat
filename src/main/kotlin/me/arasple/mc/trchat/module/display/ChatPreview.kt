package me.arasple.mc.trchat.module.display

import me.arasple.mc.trchat.api.nms.NMS
import me.arasple.mc.trchat.util.Internal
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.nms.PacketReceiveEvent

/**
 * @author wlys
 * @since 2022/6/9 21:24
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object ChatPreview {

//    val processingQueries = ConcurrentHashMap<Int, Pair<Player, String>>()

    @SubscribeEvent
    fun onReceive(e: PacketReceiveEvent) {
        if (e.packet.name == "ServerboundChatPreviewPacket") {
            val queryId = e.packet.source.invokeMethod<Int>("queryId")!!
            val query = e.packet.source.invokeMethod<String>("query")!!
//            processingQueries[queryId] = e.player to query
            NMS.INSTANCE.sendChatPreview(e.player, queryId, query)
        }
    }

//    @SubscribeEvent
//    fun onSend(e: PacketSendEvent) {
//        if (e.packet.name == "ClientboundChatPreviewPacket") {
//            val (player, message) = processingQueries[e.packet.source.invokeMethod<Int>("queryId")!!] ?: return
//            val component = player.getSession().channel?.execute(player, message, forward = false)?.first ?: return
//            val iChatBaseComponent = TrChatAPI.classChatSerializer.invokeMethod<IChatBaseComponent>("b", gson(component), fixed = true) ?: return
//            e.packet.write("preview", iChatBaseComponent)
//        }
//    }
}