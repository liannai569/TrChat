package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.nms.NMS
import me.arasple.mc.trchat.util.color.colorify
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.ItemMeta
import taboolib.module.chat.ComponentText
import taboolib.module.chat.component
import taboolib.module.nms.getI18nName
import taboolib.platform.util.isAir
import taboolib.platform.util.isNotAir
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta

fun String.parseSimple() = component().build {
    transform { it.colorify() }
}

fun ComponentText.hoverItemFixed(item: ItemStack): ComponentText {
    var newItem = item.optimizeShulkerBox()
    newItem = NMS.instance.optimizeNBT(newItem)
    return NMS.instance.hoverItem(this, newItem)
}

@Suppress("Deprecation")
fun ItemStack.optimizeShulkerBox(): ItemStack {
    try {
        if (!type.name.endsWith("SHULKER_BOX")) {
            return this
        }
        val itemClone = clone()
        val blockStateMeta = itemClone.itemMeta!! as BlockStateMeta
        val shulkerBox = blockStateMeta.blockState as ShulkerBox
        val contents = shulkerBox.inventory.contents
        val contentsClone = contents.mapNotNull {
            if (it.isNotAir()) {
                ItemStack(Material.STONE, it.amount, it.durability).modifyMeta<ItemMeta> {
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

@Suppress("Deprecation")
fun ItemStack.filter() {
    if (isAir()) return
    modifyMeta<ItemMeta> {
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