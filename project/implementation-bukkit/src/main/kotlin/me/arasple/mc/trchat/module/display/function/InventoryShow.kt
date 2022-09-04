package me.arasple.mc.trchat.module.display.function

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.arasple.mc.trchat.module.conf.file.Functions
import me.arasple.mc.trchat.module.internal.script.Reaction
import me.arasple.mc.trchat.util.passPermission
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.io.digest
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.asList
import taboolib.common.util.resettableLazy
import taboolib.common5.util.parseMillis
import taboolib.library.reflex.Reflex.Companion.invokeMethod
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
@StandardFunction
@PlatformSide([Platform.BUKKIT])
object InventoryShow : Function("INVENTORY") {

    override val alias = "Inventory-Show"

    override val reaction by resettableLazy("functions") {
        Functions.CONF["General.Inventory-Show.Action"]?.let { Reaction(it.asList()) }
    }

    @ConfigNode("General.Inventory-Show.Enabled", "function.yml")
    var enabled = true

    @ConfigNode("General.Inventory-Show.Permission", "function.yml")
    var permission = "none"

    @ConfigNode("General.Inventory-Show.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("General.Inventory-Show.Keys", "function.yml")
    var keys = listOf<String>()

    val cache: Cache<String, Inventory> = CacheBuilder.newBuilder()
        .expireAfterWrite(10L, TimeUnit.MINUTES)
        .build()

    override fun createVariable(sender: Player, message: String): String {
        return if (!enabled) {
            message
        } else {
            var result = message
            keys.forEach {
                result = result.replaceFirst(it, "{{INVENTORY:${sender.name}}}", ignoreCase = true)
            }
            return result
        }
    }

    @Suppress("Deprecation")
    override fun parseVariable(sender: Player, forward: Boolean, arg: String): Component? {
        return mirrorParse {
            val menu = buildMenu<Linked<ItemStack>>("${sender.name}'s Inventory") {
                rows(6)
                slots(inventorySlots)
                elements {
                    (9..35).map { sender.inventory.getItem(it).replaceAir() } +
                            (0..8).map { sender.inventory.getItem(it).replaceAir() }
                }
                onGenerate { _, element, _, _ ->
                    element
                }
                onBuild {
                    it.setItem(0, PLACEHOLDER_ITEM)
                    it.setItem(1, sender.inventory.invokeMethod<ItemStack>("getItemInOffHand").replaceAir())
                    it.setItem(2, buildItem(XMaterial.PLAYER_HEAD) { name = "§e${sender.name}" })
                    it.setItem(3, sender.inventory.itemInHand.replaceAir())
                    it.setItem(4, PLACEHOLDER_ITEM)
                    it.setItem(5, sender.inventory.helmet.replaceAir())
                    it.setItem(6, sender.inventory.chestplate.replaceAir())
                    it.setItem(7, sender.inventory.leggings.replaceAir())
                    it.setItem(8, sender.inventory.boots.replaceAir())
                    (9..17).forEach { slot -> it.setItem(slot, PLACEHOLDER_ITEM) }
                }
            }
            val sha1 = Base64.getEncoder().encodeToString(sender.inventory.serializeToByteArray()).digest("sha-1")
            cache.put(sha1, menu)
            sender.getComponentFromLang("Function-Inventory-Show-Format", sender.name, sha1)
        }
    }

    override fun canUse(sender: Player): Boolean {
        return sender.passPermission(permission)
    }

    private val inventorySlots = IntRange(18, 53).toList()

    private val AIR_ITEM = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = "§r" }
    private val PLACEHOLDER_ITEM = buildItem(XMaterial.WHITE_STAINED_GLASS_PANE) { name = "§r" }

    private fun ItemStack?.replaceAir() = if (isAir()) AIR_ITEM else this!!

}