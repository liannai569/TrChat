package me.arasple.mc.trchat.api.nms

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.module.display.filter.ChatFilter.filter
import me.arasple.mc.trchat.util.gson
import me.arasple.mc.trchat.util.print
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.server.v1_16_R3.NBTBase
import net.minecraft.server.v1_16_R3.NBTTagCompound
import org.bukkit.inventory.ItemStack
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion.isUniversal
import taboolib.platform.util.isNotAir

/**
 * @author Arasple
 * @date 2019/11/30 11:16
 */
class NMSImpl : NMS() {

    override fun filterIChatComponent(iChat: Any?): Any? {
        iChat ?: return null
        return try {
            val json = TrChatAPI.classChatSerializer.invokeMethod<String>("a", iChat, fixed = true)!!
            val component = filterComponent(gson(json))
            TrChatAPI.classChatSerializer.invokeMethod<IChatBaseComponent>("b", gson(component).let { if (it.length > 30000) "{\"text\":\"This chat packet is too big to send.\"}" else it }, fixed = true)!!
        } catch (t: Throwable) {
            if (!TrChat.reportedErrors.contains("filterIChatComponent")) {
                t.print("Error occurred while filtering chat component.")
                TrChat.reportedErrors.add("filterIChatComponent")
            }
            iChat
        }
    }

    override fun filterItem(item: Any?) {
        item ?: return
        kotlin.runCatching {
            val itemStack = TrChatAPI.classCraftItemStack.invokeMethod<ItemStack>("asCraftMirror", item, fixed = true)!!
            TrChatAPI.filterItemStack(itemStack)
        }
    }

    override fun filterItemList(items: Any?) {
        items ?: return
        kotlin.runCatching {
            (items as List<*>).forEach { item -> filterItem(item) }
        }.onFailure {
            kotlin.runCatching {
                (items as Array<*>).forEach { item -> filterItem(item) }
            }
        }
    }

    override fun optimizeNBT(itemStack: ItemStack, nbtWhitelist: Array<String>): ItemStack {
        try {
            val nmsItem = TrChatAPI.classCraftItemStack
                .invokeMethod<net.minecraft.server.v1_16_R3.ItemStack>("asNMSCopy", itemStack, fixed = true)!!
            if (itemStack.isNotAir() && nmsItem.hasTag()) {
                val nbtTag = NBTTagCompound()
                val mapNew = nbtTag.getProperty<HashMap<String, NBTBase>>(if (isUniversal) "tags" else "map")!!
                val mapOrigin = nmsItem.tag?.getProperty<Map<String, NBTBase>>(if (isUniversal) "tags" else "map") ?: return itemStack
                mapOrigin.entries.forEach {
                    if (nbtWhitelist.contains(it.key)) {
                        mapNew[it.key] = it.value
                    }
                }
                nmsItem.tag = nbtTag
                return TrChatAPI.classCraftItemStack.invokeMethod<ItemStack>("asBukkitCopy", nmsItem, fixed = true)!!
            }
        } catch (_: Throwable) {
        }
        return itemStack
    }

    private fun filterComponent(component: Component): Component {
        val newComponent = if (component is TextComponent && component.content().isNotEmpty()) {
            component.content(filter(component.content()).filtered)
        } else {
            component
        }
        return if (newComponent.children().isNotEmpty()) {
            Component.text { builder ->
                newComponent.children().forEach { builder.append(filterComponent(it)) }
                builder.style(newComponent.style())
                if (newComponent is TextComponent) {
                    builder.content(newComponent.content())
                }
            }
        } else {
            newComponent
        }
    }
}