package me.arasple.mc.trchat.api.nms

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.api.config.Filters
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.getSession
import me.arasple.mc.trchat.util.gson
import net.kyori.adventure.text.Component
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.PacketSendEvent

/**
 * @author Arasple
 * @date 2019/11/30 10:16
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object NMSListener {

    @SubscribeEvent(EventPriority.LOWEST)
    fun e(e: PacketSendEvent) {
        val session = e.player.getSession()
        // Chat Filter
        when (e.packet.name) {
            "ClientboundPlayerChatPacket" -> {
                // TODO: 1.19 聊天过滤
                return
            }
            "PacketPlayOutChat" -> {
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
                session.addMessage(e.packet)
                if (!Filters.CONF.getBoolean("Enable.Chat") || !session.isFilterEnabled) {
                    return
                }
                if (!TrChat.paperEnv) {
                    e.packet.write("a", NMS.INSTANCE.filterIChatComponent(e.packet.read<Any>("a")))
                } else {
                    e.packet.write("adventure\$message", filterComponentWithValidating(e.packet.read<Component>("adventure\$message")))
                }
//                kotlin.runCatching {
//                    val components = e.packet.read<Array<BaseComponent>>("components") ?: return
//                    e.packet.write("components", components.map { filterComponent(it) }.toTypedArray())
//                }
                return
            }
            "PacketPlayOutWindowItems" -> {
                if (!Filters.CONF.getBoolean("Enable.Item") || !session.isFilterEnabled) {
                    return
                }
                if (majorLegacy >= 11700) {
                    NMS.INSTANCE.filterItemList(e.packet.read<Any>("items"))
                } else {
                    NMS.INSTANCE.filterItemList(e.packet.read<Any>("b"))
                }
                return
            }
            "PacketPlayOutSetSlot" -> {
                if (!Filters.CONF.getBoolean("Enable.Item") || !session.isFilterEnabled) {
                    return
                }
                if (majorLegacy >= 11700) {
                    NMS.INSTANCE.filterItem(e.packet.read<Any>("itemStack"))
                } else {
                    NMS.INSTANCE.filterItem(e.packet.read<Any>("c"))
                }
                return
            }
        }
        // Tab Complete
//        if (TrChatFiles.settings.getBoolean("GENERAL.PREVENT-TAB-COMPLETE", false)
//            && e.packet.name == "PacketPlayOutTabComplete"
//            && !e.player.hasPermission("trchat.bypass.tabcomplete")) {
//            if (majorLegacy >= 11700) {
//                e.isCancelled = (e.packet.read<Suggestions>("suggestions") ?: Suggestions.empty().get())
//                    .list.none { Bukkit.getPlayerExact(it.text) != null }
//            } else if (majorLegacy >= 11300) {
//                e.isCancelled = (e.packet.read<Suggestions>("b") ?: Suggestions.empty().get())
//                    .list.none { Bukkit.getPlayerExact(it.text) != null }
//            } else {
//                e.isCancelled = listOf(*e.packet.read<Array<String>>("a") ?: emptyArray())
//                    .none { Bukkit.getPlayerExact(it) != null }
//            }
//        }
    }

//    private fun filterComponent(component: BaseComponent): BaseComponent {
//        if (component is TextComponent && component.text.isNotEmpty()) {
//            component.text = filter(component.text).filtered
//        }
//        if (!component.extra.isNullOrEmpty()) {
//            component.extra = component.extra.map { filterComponent(it) }
//        }
//        return component
//    }

    private fun filterComponentWithValidating(component: Component?): Component? {
        return TrChatAPI.filterComponent(component)?.let {
            if (gson(it).length >= 32000) {
                Component.text("This chat packet is too big to send.")
            } else {
                it
            }
        }
    }
}