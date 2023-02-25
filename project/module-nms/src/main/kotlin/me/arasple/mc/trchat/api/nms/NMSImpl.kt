package me.arasple.mc.trchat.api.nms

import me.arasple.mc.trchat.util.reportOnce
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.server.v1_12_R1.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.module.chat.ComponentText
import taboolib.module.nms.MinecraftVersion.isUniversal
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.sendPacket
import taboolib.platform.util.isNotAir
import java.util.*

private typealias CraftItemStack19 = org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
private typealias CraftChatMessage19 = org.bukkit.craftbukkit.v1_19_R2.util.CraftChatMessage
private typealias NMSNBTTagCompound = net.minecraft.nbt.NBTTagCompound
private typealias NMSIChatBaseComponent = net.minecraft.network.chat.IChatBaseComponent
private typealias NMSChatSerializer = IChatBaseComponent.ChatSerializer

/**
 * @author Arasple
 * @date 2019/11/30 11:16
 */
@Suppress("unused")
class NMSImpl : NMS() {

    override fun craftChatMessageFromComponent(component: ComponentText): Any {
        return try {
            if (majorLegacy >= 11604) {
                CraftChatMessage19.fromJSON(component.toRawMessage())
            } else {
                NMSChatSerializer.a(component.toRawMessage())!!
            }
        } catch (t: Throwable) {
            throw IllegalStateException("Got an error translating component!Please report!", t)
        }
    }

    override fun rawMessageFromCraftChatMessage(component: Any): String {
        return try {
            if (majorLegacy >= 11604) {
                CraftChatMessage19.toJSON(component as NMSIChatBaseComponent)
            } else {
                NMSChatSerializer.a(component as IChatBaseComponent)!!
            }
        } catch (t: Throwable) {
            throw IllegalStateException("Got an error translating component!Please report!", t)
        }
    }

    override fun sendMessage(receiver: Player, component: ComponentText, sender: UUID?) {
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

    override fun hoverItem(component: ComponentText, itemStack: ItemStack): ComponentText {
        val nmsItem = CraftItemStack19.asNMSCopy(itemStack)
        val nbtTag = NMSNBTTagCompound()
        nmsItem.save(nbtTag)
        val id = nbtTag.getString("id") ?: "minecraft:air"
        val nbt = nbtTag.get("tag")?.toString() ?: "{}"
        return component.hoverItem(id, nbt)
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

    override fun addCustomChatCompletions(player: Player, entries: List<String>) {
        if (majorLegacy < 11901) return
        player.sendPacket(ClientboundCustomChatCompletionsPacket::class.java.invokeConstructor(
            ClientboundCustomChatCompletionsPacket.a.ADD,
            entries
        ))
    }

    override fun removeCustomChatCompletions(player: Player, entries: List<String>) {
        if (majorLegacy < 11901) return
        player.sendPacket(ClientboundCustomChatCompletionsPacket::class.java.invokeConstructor(
            ClientboundCustomChatCompletionsPacket.a.REMOVE,
            entries
        ))
    }
}