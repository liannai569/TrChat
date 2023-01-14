package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.nms.NMS
import me.arasple.mc.trchat.module.conf.file.Filters
import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.util.data
import me.arasple.mc.trchat.util.session
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.PacketSendEvent

/**
 * @author Arasple
 * @date 2019/11/30 10:16
 */
@PlatformSide([Platform.BUKKIT])
object ListenerPackets {

    @SubscribeEvent
    fun secure(e: PacketSendEvent) {
        if (majorLegacy == 11902) {
            when (e.packet.name) {
                "ClientboundServerDataPacket" -> e.packet.write("enforcesSecureChat", true)
                "ClientboundPlayerChatHeaderPacket" -> e.isCancelled = true
            }
        }
    }

    @SubscribeEvent
    fun recall(e: PacketSendEvent) {
        val session = e.player.session
        when (e.packet.name) {
            "ClientboundSystemChatPacket" -> {
                session.addMessage(e.packet)
            }
            "PacketPlayOutChat" -> {
                if (Settings.CONF.getString("Options.Send-Message-Method", "CHAT")?.uppercase() == "CHAT") {
                    val type = if (majorLegacy >= 11700) {
                        e.packet.read<Any>("type")!!.getProperty<Byte>("index")
                    } else if (majorLegacy >= 11200) {
                        e.packet.read<Any>("b")!!.getProperty<Byte>("d")
                    } else {
                        e.packet.read<Byte>("b")
                    }
                    if (type != 0.toByte()) {
                        return
                    }
                }
                session.addMessage(e.packet)
            }
        }
    }

    @SubscribeEvent(EventPriority.LOWEST)
    fun filter(e: PacketSendEvent) {
        val data = e.player.data
        when (e.packet.name) {
            "PacketPlayOutWindowItems" -> {
                if (!Filters.CONF.getBoolean("Enable.Item") || !data.isFilterEnabled) {
                    return
                }
                if (majorLegacy >= 11700) {
                    NMS.INSTANCE.filterItemList(e.packet.read<Any>("items"))
                } else {
                    NMS.INSTANCE.filterItemList(e.packet.read<Any>("b"))
                }
            }
            "PacketPlayOutSetSlot" -> {
                if (!Filters.CONF.getBoolean("Enable.Item") || !data.isFilterEnabled) {
                    return
                }
                if (majorLegacy >= 11700) {
                    NMS.INSTANCE.filterItem(e.packet.read<Any>("itemStack"))
                } else {
                    NMS.INSTANCE.filterItem(e.packet.read<Any>("c"))
                }
            }
        }
    }

}