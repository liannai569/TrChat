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
import taboolib.common.reflect.Reflex.Companion.invokeMethod
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
object InventoryShow {

    @ConfigNode("General.Inventory-Show.Enabled", "function.yml")
    var enabled = true

    @ConfigNode("General.Inventory-Show.Permission", "function.yml")
    var permission = "null"

    @ConfigNode("General.Inventory-Show.Format", "function.yml")
    var format = "&8[&3{0}'s Inventory&8]"

    @ConfigNode("General.Inventory-Show.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("General.Inventory-Show.Keys", "function.yml")
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
                result = result.replaceFirst(it, "{{INVENTORY:SELF}}", ignoreCase = true)
            }
            return result
        }
    }

    fun createComponent(player: Player): Component {
        return mirrorNow("Function:InventoryShow:CreateComponent") {
            val menu = buildMenu<Linked<ItemStack>>("${player.name}'s Inventory") {
                rows(6)
                slots(inventorySlots)
                elements {
                    (9..35).map { player.inventory.getItem(it).replaceAir() } +
                            (0..8).map { player.inventory.getItem(it).replaceAir() }
                }
                onGenerate { _, element, _, _ ->
                    element
                }
                onBuild {
                    it.setItem(0, PLACEHOLDER_ITEM)
                    it.setItem(1, player.inventory.invokeMethod<ItemStack>("getItemInOffHand").replaceAir())
                    it.setItem(2, buildItem(XMaterial.PLAYER_HEAD) { name = "§e${player.name}" })
                    it.setItem(3, player.inventory.itemInHand.replaceAir())
                    it.setItem(4, PLACEHOLDER_ITEM)
                    it.setItem(5, player.inventory.helmet.replaceAir())
                    it.setItem(6, player.inventory.chestplate.replaceAir())
                    it.setItem(7, player.inventory.leggings.replaceAir())
                    it.setItem(8, player.inventory.boots.replaceAir())
                    (9..17).forEach { slot -> it.setItem(slot, PLACEHOLDER_ITEM) }
                }
            }
            val sha1 = Base64.getEncoder().encodeToString(player.inventory.serializeToByteArray()).digest("sha-1")
            cache.put(sha1, menu)
            legacy(format.replaceWithOrder(player.name).colorify()).clickEvent(ClickEvent.runCommand("/view-inventory $sha1"))
        }
    }

    private val inventorySlots = IntRange(18, 53).toList()

    private val AIR_ITEM = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = "§r" }
    private val PLACEHOLDER_ITEM = buildItem(XMaterial.WHITE_STAINED_GLASS_PANE) { name = "§r" }

    private fun ItemStack?.replaceAir() = if (isAir()) AIR_ITEM else this!!
}