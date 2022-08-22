package me.arasple.mc.trchat.api.nms

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.internal.BukkitComponentManager
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.util.*
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundChatPreviewPacket
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.server.v1_16_R3.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion.isUniversal
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.sendPacket
import taboolib.platform.util.isAir
import taboolib.platform.util.isNotAir
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta
import java.util.*

/**
 * @author Arasple
 * @date 2019/11/30 11:16
 */
@Suppress("unused")
class NMSImpl : NMS() {

    override fun filterIChatComponent(iChat: Any?): Any? {
        iChat ?: return null
        return try {
            val json = classChatSerializer.invokeMethod<String>("a", iChat, isStatic = true)!!
            val component = BukkitComponentManager.filterComponent(gson(json), 32000)
            component.toIChatBaseComponent()
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
            val itemStack = classCraftItemStack.invokeMethod<ItemStack>("asCraftMirror", item, isStatic = true)!!
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
            val nmsItem = classCraftItemStack.invokeMethod<net.minecraft.server.v1_16_R3.ItemStack>("asNMSCopy", itemStack, isStatic = true)!!
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
                return classCraftItemStack.invokeMethod<ItemStack>("asBukkitCopy", nmsItem, isStatic = true)!!
            }
        } catch (_: Throwable) {
        }
        return itemStack
    }

    override fun sendChatPreview(player: Player, queryId: Int, query: String) {
        val component = player.session.getChannel()?.execute(player, query, forward = false)?.first ?: return
        player.sendPacket(ClientboundChatPreviewPacket::class.java.invokeConstructor(queryId, component.toIChatBaseComponent()))
    }

    override fun sendPlayerChatMessage(receiver: Player, component: Component, sender: UUID?) {
        if (majorLegacy >= 11901) {
            receiver.sendPacket(ClientboundSystemChatPacket::class.java.invokeConstructor(
                component.toIChatBaseComponent(),
                false
            ))
        } else if (majorLegacy == 11900) {
            receiver.sendPacket(ClientboundSystemChatPacket::class.java.invokeConstructor(
                component.toIChatBaseComponent(),
                0
            ))
        } else if (majorLegacy >= 11600) {
            receiver.sendPacket(PacketPlayOutChat::class.java.invokeConstructor(
                component.toIChatBaseComponent(),
                ChatMessageType.CHAT,
                sender
            ))
        } else {
            receiver.sendPacket(PacketPlayOutChat::class.java.invokeConstructor(
                component.toIChatBaseComponent(),
                ChatMessageType.CHAT
            ))
        }
    }

    override fun sendSystemChatMessage(receiver: Player, component: Component, sender: UUID?) {
        if (majorLegacy >= 11901) {
            receiver.sendPacket(ClientboundSystemChatPacket::class.java.invokeConstructor(
                component.toIChatBaseComponent(),
                false
            ))
        } else if (majorLegacy == 11900) {
            receiver.sendPacket(ClientboundSystemChatPacket::class.java.invokeConstructor(
                component.toIChatBaseComponent(),
                0
            ))
        } else if (majorLegacy >= 11600) {
            receiver.sendPacket(PacketPlayOutChat::class.java.invokeConstructor(
                component.toIChatBaseComponent(),
                ChatMessageType.SYSTEM,
                sender
            ))
        } else {
            receiver.sendPacket(PacketPlayOutChat::class.java.invokeConstructor(
                component.toIChatBaseComponent(),
                ChatMessageType.SYSTEM
            ))
        }
    }

    @Suppress("Deprecation")
    private fun filterItemStack(itemStack: ItemStack) {
        if (itemStack.isAir()) {
            return
        }
        itemStack.modifyMeta<ItemMeta> {
            if (hasDisplayName()) {
                setDisplayName(TrChat.api().getFilterManager().filter(displayName).filtered)
            }
            modifyLore {
                if (isNotEmpty()) {
                    replaceAll { TrChat.api().getFilterManager().filter(it).filtered }
                }
            }
        }
    }

    private fun Component.toIChatBaseComponent() = if (majorLegacy >= 10900) {
        classChatSerializer.invokeMethod<IChatBaseComponent>("b", gson(this), isStatic = true)!!
    } else {
        classChatSerializer.invokeMethod<IChatBaseComponent>("a", gson(this), isStatic = true)!!
    }

}