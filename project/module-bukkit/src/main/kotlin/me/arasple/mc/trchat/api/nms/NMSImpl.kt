package me.arasple.mc.trchat.api.nms

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.internal.BukkitComponentManager
import me.arasple.mc.trchat.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.minecraft.network.protocol.game.ClientboundChatPreviewPacket
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.server.v1_16_R3.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.module.nms.MinecraftVersion.isUniversal
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.sendPacket
import taboolib.platform.util.isAir
import taboolib.platform.util.isNotAir
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta
import java.util.*

private typealias CraftItemStack19 = org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
private typealias CraftChatMessage19 = org.bukkit.craftbukkit.v1_19_R2.util.CraftChatMessage
private typealias NMSItemStack = net.minecraft.world.item.ItemStack
private typealias NMSNBTTagCompound = net.minecraft.nbt.NBTTagCompound
private typealias NMSIChatBaseComponent = net.minecraft.network.chat.IChatBaseComponent
private typealias NMSChatSerializer = net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer

/**
 * @author Arasple
 * @date 2019/11/30 11:16
 */
@Suppress("unused")
class NMSImpl : NMS() {

    override fun craftChatMessageFromComponent(component: Component): Any {
        return try {
            if (majorLegacy >= 11604) {
                CraftChatMessage19.fromJSON(gson(component))
            } else {
                NMSChatSerializer.a(gson(component))!!
            }
        } catch (t: Throwable) {
            throw IllegalStateException("Got an error translating component!Please report!", t)
        }
    }

    override fun sendPlayerChatMessage(receiver: Player, component: Component, sender: UUID?) {
        if (majorLegacy >= 11901) {
            receiver.sendPacket(ClientboundSystemChatPacket::class.java.invokeConstructor(
                craftChatMessageFromComponent(component),
                false
            ))
        } else if (majorLegacy == 11900) {
            receiver.sendPacket(ClientboundSystemChatPacket::class.java.invokeConstructor(
                craftChatMessageFromComponent(component),
                0
            ))
        } else if (majorLegacy >= 11600) {
            receiver.sendPacket(PacketPlayOutChat::class.java.invokeConstructor(
                craftChatMessageFromComponent(component),
                ChatMessageType.CHAT,
                sender
            ))
        } else if (majorLegacy >= 11200) {
            receiver.sendPacket(PacketPlayOutChat::class.java.invokeConstructor(
                craftChatMessageFromComponent(component),
                ChatMessageType.CHAT
            ))
        } else {
            receiver.sendPacket(PacketPlayOutChat::class.java.invokeConstructor(
                craftChatMessageFromComponent(component),
                0.toByte()
            ))
        }
    }

    override fun sendSystemChatMessage(receiver: Player, component: Component, sender: UUID?) {
        if (majorLegacy >= 11901) {
            receiver.sendPacket(ClientboundSystemChatPacket::class.java.invokeConstructor(
                craftChatMessageFromComponent(component),
                false
            ))
        } else if (majorLegacy == 11900) {
            receiver.sendPacket(ClientboundSystemChatPacket::class.java.invokeConstructor(
                craftChatMessageFromComponent(component),
                0
            ))
        } else if (majorLegacy >= 11600) {
            receiver.sendPacket(PacketPlayOutChat::class.java.invokeConstructor(
                craftChatMessageFromComponent(component),
                ChatMessageType.SYSTEM,
                sender
            ))
        } else if (majorLegacy >= 11200) {
            receiver.sendPacket(PacketPlayOutChat::class.java.invokeConstructor(
                craftChatMessageFromComponent(component),
                ChatMessageType.SYSTEM
            ))
        } else {
            receiver.sendPacket(PacketPlayOutChat::class.java.invokeConstructor(
                craftChatMessageFromComponent(component),
                1.toByte()
            ))
        }
    }

    override fun generateHoverItemEvent(itemStack: ItemStack): HoverEvent<HoverEvent.ShowItem> {
        val nmsItem = CraftItemStack19.asNMSCopy(itemStack)
        val nbtTag = NMSNBTTagCompound()
        nmsItem.save(nbtTag)
        val id = nbtTag.getString("id") ?: "minecraft:air"
        val tag = nbtTag.get("tag")?.toString() ?: "{}"
        val nbt = try {
            BinaryTagHolder.binaryTagHolder(tag)
        } catch (_: Throwable) {
            @Suppress("Deprecation")
            BinaryTagHolder.of(tag)
        }
        return HoverEvent.showItem(Key.key(id), itemStack.amount, nbt)
    }

    override fun optimizeNBT(itemStack: ItemStack, nbtWhitelist: Array<String>): ItemStack {
        try {
            val nmsItem = CraftItemStack19.asNMSCopy(itemStack)
            if (itemStack.isNotAir() && nmsItem.hasTag()) {
                if (isUniversal) {
                    val nbtTag = NMSNBTTagCompound()
                    nmsItem.tag!!.allKeys.forEach {
                        if (it in nbtWhitelist) {
                            nbtTag.put(it, nmsItem.tag!!.get(it))
                        }
                    }
                    nmsItem.tag = nbtTag
                    return CraftItemStack19.asBukkitCopy(nmsItem)
                }
            }
        } catch (t: Throwable) {
            t.reportOnce("Got an error optimizing item nbt")
        }
        return itemStack
    }

    override fun filterItem(item: Any?) {
        item ?: return
        kotlin.runCatching {
            val craftItem = CraftItemStack19.asCraftMirror(item as NMSItemStack)
            filterItemStack(craftItem)
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

    @Deprecated("Use TrChat.api().getComponentManager().filterComponent()")
    override fun filterIChatComponent(iChat: Any?): Any? {
        iChat ?: return null
        return try {
            val json = CraftChatMessage19.toJSON(iChat as NMSIChatBaseComponent)
            val component = BukkitComponentManager.filterComponent(gson(json), 32000)
            craftChatMessageFromComponent(component)
        } catch (t: Throwable) {
            t.reportOnce("Got an error filtering minecraft chat component.")
            iChat
        }
    }

    @Deprecated("Removed since 1.19.3")
    override fun sendChatPreview(player: Player, queryId: Int, query: String) {
        val component = player.session.getChannel()?.execute(player, query, forward = false)?.first ?: return
        player.sendPacket(ClientboundChatPreviewPacket::class.java.invokeConstructor(
            queryId,
            craftChatMessageFromComponent(component)
        ))
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

}