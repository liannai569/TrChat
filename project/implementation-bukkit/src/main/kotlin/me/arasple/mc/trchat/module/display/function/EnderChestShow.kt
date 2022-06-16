package me.arasple.mc.trchat.module.display.function

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.arasple.mc.trchat.util.color.colorify
import me.arasple.mc.trchat.util.legacy
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.io.digest
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.replaceWithOrder
import taboolib.common5.mirrorNow
import taboolib.common5.util.parseMillis
import taboolib.library.xseries.XMaterial
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir
import taboolib.platform.util.serializeToByteArray
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author wlys
 * @since 2022/3/18 19:14
 */
@PlatformSide([Platform.BUKKIT])
object EnderChestShow {

    @ConfigNode("General.EnderChest-Show.Enabled", "function.yml")
    var enabled = true

    @ConfigNode("General.EnderChest-Show.Permission", "function.yml")
    var permission = "null"

    @ConfigNode("General.EnderChest-Show.Format", "function.yml")
    var format = "&8[&3{0}'s Inventory&8]"

    @ConfigNode("General.EnderChest-Show.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("General.EnderChest-Show.Keys", "function.yml")
    var keys = listOf<String>()

    val cache: Cache<String, Inventory> = CacheBuilder.newBuilder()
        .expireAfterWrite(10L, TimeUnit.MINUTES)
        .build()

    fun replaceMessage(message: String): String {
        return if (!enabled) {
            message
        } else {
            var result = message
            keys.forEach {
                result = result.replaceFirst(it, "{{ENDERCHEST:SELF}}", ignoreCase = true)
            }
            return result
        }
    }

    fun createComponent(player: Player): Component {
        return mirrorNow("Function:EnderChestShow:CreateComponent") {
            val menu = buildMenu<Linked<ItemStack>>("${player.name}'s Ender Chest") {
                rows(3)
                slots(inventorySlots)
                elements {
                    (0..26).map { player.enderChest.getItem(it).replaceAir() }
                }
                onGenerate { _, element, _, _ ->
                    element
                }
            }
            val sha1 = Base64.getEncoder().encodeToString(player.inventory.serializeToByteArray()).digest("sha-1")
            cache.put(sha1, menu)
            legacy(format.replaceWithOrder(player.name).colorify()).clickEvent(ClickEvent.runCommand("/view-enderchest $sha1"))
        }
    }

    private val inventorySlots = IntRange(0, 26).toList()

    private val AIR_ITEM = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = "§r" }

    private fun ItemStack?.replaceAir() = if (isAir()) AIR_ITEM else this!!
}