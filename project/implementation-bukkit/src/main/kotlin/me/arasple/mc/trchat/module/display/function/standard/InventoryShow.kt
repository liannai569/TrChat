package me.arasple.mc.trchat.module.display.function.standard

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.arasple.mc.trchat.module.conf.file.Functions
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.display.function.StandardFunction
import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyManager
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
import taboolib.common5.util.encodeBase64
import taboolib.common5.util.parseMillis
import taboolib.library.xseries.XMaterial
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.nms.MinecraftVersion
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir
import taboolib.platform.util.serializeToByteArray

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
        .maximumSize(10)
        .build()

    private val inventorySlots = IntRange(18, 53).toList()
    private val AIR_ITEM = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = "§r" }
    private val PLACEHOLDER_ITEM = buildItem(XMaterial.WHITE_STAINED_GLASS_PANE) { name = "§r" }

    override fun createVariable(sender: Player, message: String): String {
        return if (!enabled) {
            message
        } else {
            var result = message
            keys.forEach {
                result = result.replaceFirst(it, "{{INVENTORY:${sender.name}}}", ignoreCase = true)
            }
            result
        }
    }

    @Suppress("Deprecation")
    override fun parseVariable(sender: Player, forward: Boolean, arg: String): Component? {
        return mirrorParse {
            computeAndCache(
                sender.name,
                sender.inventory,
                sender.inventory.itemInHand,
                sender.inventory.runCatching { itemInOffHand }.getOrDefault(AIR_ITEM)
            ).let {
                if (forward) {
                    BukkitProxyManager.sendTrChatMessage(
                        sender,
                        "InventoryShow",
                        MinecraftVersion.minecraftVersion,
                        sender.name,
                        it.first,
                        it.second
                    )
                }
                sender.getComponentFromLang("Function-Inventory-Show-Format", sender.name, it.first)
            }
        }
    }

    override fun canUse(sender: Player): Boolean {
        return sender.passPermission(permission)
    }

    fun computeAndCache(
        name: String,
        inventory: Inventory,
        mainHand: ItemStack?,
        offHand: ItemStack?
    ): Pair<String, String> {
        val sha1 = inventory.serializeToByteArray().encodeBase64().digest("sha-1")
        if (cache.getIfPresent(sha1) != null) {
            return sha1 to cache.getIfPresent(sha1)!!.serializeToByteArray().encodeBase64()
        }
        val menu = buildMenu<Linked<ItemStack>>("$name's Inventory") {
            rows(6)
            slots(inventorySlots)
            elements {
                (9..35).map { inventory.getItem(it).replaceAir() } +
                        (0..8).map { inventory.getItem(it).replaceAir() }
            }
            onGenerate { _, element, _, _ -> element }
            onBuild { _, inv ->
                inv.setItem(0, PLACEHOLDER_ITEM)
                inv.setItem(1, offHand.replaceAir())
                inv.setItem(2, buildItem(XMaterial.PLAYER_HEAD) { this.name = "§e$name" })
                inv.setItem(3, mainHand.replaceAir())
                inv.setItem(4, PLACEHOLDER_ITEM)
                inv.setItem(5, inventory.getItem(inventory.size - 2).replaceAir())
                inv.setItem(6, inventory.getItem(inventory.size - 3).replaceAir())
                inv.setItem(7, inventory.getItem(inventory.size - 4).replaceAir())
                inv.setItem(8, inventory.getItem(inventory.size - 5).replaceAir())
                (9..17).forEach { slot -> inv.setItem(slot, PLACEHOLDER_ITEM) }
            }
            onClick(lock = true)
        }
        cache.put(sha1, menu)
        return sha1 to menu.serializeToByteArray().encodeBase64()
    }

    private fun ItemStack?.replaceAir() = if (isAir()) AIR_ITEM else this!!

}