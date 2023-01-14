package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.api.nms.NMS
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.util.color.colorify
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.util.unsafeLazy
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.getI18nName
import taboolib.platform.util.isNotAir
import taboolib.platform.util.modifyMeta

/**
 * @author ItsFlicker
 * @since 2022/6/19 10:16
 */
private val legacySerializer by unsafeLazy {
    if (TrChatBukkit.isPaperEnv) {
        LegacyComponentSerializer.legacySection()
    } else {
        LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build()
    }
}

private val gsonSerializer by unsafeLazy {
    if (TrChatBukkit.isPaperEnv) {
        GsonComponentSerializer.gson()
    } else {
        GsonComponentSerializer.builder()
            .legacyHoverEventSerializer(
                Class.forName("net.kyori.adventure.text.serializer.gson.legacyimpl.NBTLegacyHoverEventSerializer")
                    .invokeMethod<LegacyHoverEventSerializer>("get", isStatic = true)
            )
            .also { if (MinecraftVersion.majorLegacy < 11600) it.emitLegacyHoverEvent() }
            .build()
    }
}

val miniMessage = MiniMessage.miniMessage()

fun legacy(component: Component) = legacySerializer.serialize(component)

fun legacy(string: String) = legacySerializer.deserialize(string)

fun gson(component: Component) = gsonSerializer.serialize(component)

fun gson(string: String) = gsonSerializer.deserialize(string)

fun String.legacyColorToTag(): String {
    if (!contains('&') && !contains('§')) return this
    val chars = colorify().toCharArray()
    return buildString {
        var i = 0
        while (i < chars.size) {
            if (chars[i] == '§') {
                if (i + 1 < chars.size) {
                    when (chars[i + 1]) {
                        '0' -> { append("<black>"); i++ }
                        '1' -> { append("<dark_blue>"); i++ }
                        '2' -> { append("<dark_green>"); i++ }
                        '3' -> { append("<dark_aqua>"); i++ }
                        '4' -> { append("<dark_red>"); i++ }
                        '5' -> { append("<dark_purple>"); i++ }
                        '6' -> { append("<gold>"); i++ }
                        '7' -> { append("<gray>"); i++ }
                        '8' -> { append("<dark_gray>"); i++ }
                        '9' -> { append("<blue>"); i++ }
                        'a' -> { append("<green>"); i++ }
                        'b' -> { append("<aqua>"); i++ }
                        'c' -> { append("<red>"); i++ }
                        'd' -> { append("<light_purple>"); i++ }
                        'e' -> { append("<yellow>"); i++ }
                        'f' -> { append("<white>"); i++ }
                        'l' -> { append("<b>"); i++ }
                        'm' -> { append("<st>"); i++ }
                        'o' -> { append("<i>"); i++ }
                        'n' -> { append("<u>"); i++ }
                        'k' -> { append("<obf>"); i++ }
                        'r' -> { append("<reset>"); i++ }
                        'x' -> {
                            append("<#")
                            append(chars[i + 3])
                            append(chars[i + 5])
                            append(chars[i + 7])
                            append(chars[i + 9])
                            append(chars[i + 11])
                            append(chars[i + 13])
                            append(">")
                            i += 13
                        }
                    }
                }
            } else {
                append(chars[i])
            }
            i++
        }
    }
}

fun Component.hoverItemFixed(item: ItemStack): Component {
    var newItem = item.optimizeShulkerBox()
    newItem = NMS.INSTANCE.optimizeNBT(newItem)
    return hoverEvent(NMS.INSTANCE.generateHoverItemEvent(newItem))
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