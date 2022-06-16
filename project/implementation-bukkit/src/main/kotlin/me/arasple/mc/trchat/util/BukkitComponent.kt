package me.arasple.mc.trchat.util

import io.papermc.paper.text.PaperComponents
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.api.nms.NMS
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.nms.getI18nName
import taboolib.module.nms.nmsClass
import taboolib.platform.util.isNotAir
import taboolib.platform.util.modifyMeta

/**
 * @author wlys
 * @since 2022/2/4 12:54
 */
private val classNBTTagCompound by lazy {
    nmsClass("NBTTagCompound")
}

private val LEGACY_SERIALIZER by lazy {
    if (TrChat.paperEnv) {
        PaperComponents.legacySectionSerializer()
    } else {
        BukkitComponentSerializer.legacy()
    }
}

private val GSON_SERIALIZER by lazy {
    if (TrChat.paperEnv) {
        PaperComponents.gsonSerializer()
    } else {
        BukkitComponentSerializer.gson()
    }
}

fun legacy(component: Component) = LEGACY_SERIALIZER.serialize(component)

fun legacy(string: String) = LEGACY_SERIALIZER.deserialize(string)

fun gson(component: Component) = GSON_SERIALIZER.serialize(component)

fun gson(string: String) = GSON_SERIALIZER.deserialize(string)

fun TextComponent.hoverItemFixed(item: ItemStack, player: Player): TextComponent {
    var newItem = item.optimizeShulkerBox()
    newItem = NMS.INSTANCE.optimizeNBT(newItem)
    newItem = HookPlugin.getEcoEnchants().displayItem(newItem, player)
    if (TrChat.paperEnv) {
        return hoverEvent(newItem.asHoverEvent())
    }
    val nmsItemStack = TrChatAPI.classCraftItemStack.invokeMethod<Any>("asNMSCopy", newItem, fixed = true)!!
    val nmsNBTTabCompound = classNBTTagCompound.invokeConstructor()
    val itemJson = nmsItemStack.invokeMethod<Any>("save", nmsNBTTabCompound)!!
    val id = itemJson.invokeMethod<String>("getString", "id") ?: "minecraft:air"
    val tag = itemJson.invokeMethod<Any>("get", "tag")?.toString() ?: "{}"
    return hoverEvent(HoverEvent.showItem(Key.key(id), newItem.amount, BinaryTagHolder.binaryTagHolder(tag)))
}

private fun ItemStack.optimizeShulkerBox(): ItemStack {
    try {
        if (!type.name.endsWith("SHULKER_BOX")) {
            return this
        }
        val itemClone = clone()
        val blockStateMeta = itemClone.itemMeta!! as BlockStateMeta
        val shulkerBox = blockStateMeta.blockState as ShulkerBox
        val contents = shulkerBox.inventory.contents ?: return this
        val contentsClone = contents.mapNotNull {
            if (it.isNotAir()) {
                ItemStack(Material.STONE, it!!.amount, it.durability).modifyMeta<ItemMeta> {
                    if (it.itemMeta?.hasDisplayName() == true) {
                        setDisplayName(it.itemMeta!!.displayName)
                    } else {
                        setDisplayName(it.getI18nName())
                    }
                }
            } else {
                null
            }
        }.toTypedArray()
        shulkerBox.inventory.contents = contentsClone
        blockStateMeta.blockState = shulkerBox
        itemClone.itemMeta = blockStateMeta
        return itemClone
    } catch (_: Throwable) {
    }
    return this
}