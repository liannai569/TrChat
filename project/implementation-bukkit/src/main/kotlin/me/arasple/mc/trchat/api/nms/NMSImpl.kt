package me.arasple.mc.trchat.api.nms

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.internal.BukkitComponentManager
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.util.getSession
import me.arasple.mc.trchat.util.gson
import me.arasple.mc.trchat.util.print
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.protocol.game.ClientboundChatPreviewPacket
import net.minecraft.server.v1_16_R3.NBTBase
import net.minecraft.server.v1_16_R3.NBTTagCompound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion.isUniversal
import taboolib.module.nms.sendPacket
import taboolib.platform.util.isAir
import taboolib.platform.util.isNotAir
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta

/**
 * @author Arasple
 * @date 2019/11/30 11:16
 */
@Suppress("unused")
class NMSImpl : NMS() {

    override fun filterIChatComponent(iChat: Any?): Any? {
        iChat ?: return null
        return try {
            val json = TrChatBukkit.classChatSerializer.invokeMethod<String>("a", iChat, fixed = true)!!
            val component = BukkitComponentManager.filterComponent(gson(json), 32000)!!
            TrChatBukkit.classChatSerializer.invokeMethod<IChatBaseComponent>("b", gson(component), fixed = true)!!
        } catch (t: Throwable) {
            if (!TrChatBukkit.reportedErrors.contains("filterIChatComponent")) {
                t.print("Error occurred while filtering chat component.")
                TrChatBukkit.reportedErrors.add("filterIChatComponent")
            }
            iChat
        }
    }

    override fun filterItem(item: Any?) {
        item ?: return
        kotlin.runCatching {
            val itemStack = TrChatBukkit.classCraftItemStack.invokeMethod<ItemStack>("asCraftMirror", item, fixed = true)!!
            filterItemStack(itemStack)
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
            val nmsItem = TrChatBukkit.classCraftItemStack
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
                return TrChatBukkit.classCraftItemStack.invokeMethod<ItemStack>("asBukkitCopy", nmsItem, fixed = true)!!
            }
        } catch (_: Throwable) {
        }
        return itemStack
    }

    override fun sendChatPreview(player: Player, queryId: Int, query: String) {
        val component = player.getSession().getChannel()?.execute(player, query, forward = false)?.first ?: return
        val iChatBaseComponent = TrChatBukkit.classChatSerializer.invokeMethod<IChatBaseComponent>("b", gson(component), fixed = true)
        player.sendPacket(ClientboundChatPreviewPacket(queryId, iChatBaseComponent))
    }

    @Suppress("Deprecation")
    private fun filterItemStack(itemStack: ItemStack) {
        if (itemStack.isAir()) {
            return
        }
        itemStack.modifyMeta<ItemMeta> {
            if (hasDisplayName()) {
                setDisplayName(TrChat.api().filter(displayName).filtered)
            }
            modifyLore {
                if (isNotEmpty()) {
                    replaceAll { TrChat.api().filter(it).filtered }
                }
            }
        }
    }
}