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
import taboolib.library.xseries.XMaterial
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir
import taboolib.platform.util.serializeToByteArray
import java.util.*

/**
 * @author wlys
 * @since 2022/3/18 19:14
 */
@StandardFunction
@PlatformSide([Platform.BUKKIT])
object EnderChestShow : Function("ENDERCHEST") {

    override val alias = "EnderChest-Show"

    override val reaction by resettableLazy("functions") {
        Functions.CONF["General.EnderChest-Show.Action"]?.let { Reaction(it.asList()) }
    }

    @ConfigNode("General.EnderChest-Show.Enabled", "function.yml")
    var enabled = true

    @ConfigNode("General.EnderChest-Show.Permission", "function.yml")
    var permission = "none"

    @ConfigNode("General.EnderChest-Show.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("General.EnderChest-Show.Keys", "function.yml")
    var keys = listOf<String>()

    val cache: Cache<String, Inventory> = CacheBuilder.newBuilder()
        .maximumSize(10)
        .build()

    override fun createVariable(sender: Player, message: String): String {
        return if (!enabled) {
            message
        } else {
            var result = message
            keys.forEach {
                result = result.replaceFirst(it, "{{ENDERCHEST:${sender.name}}}", ignoreCase = true)
            }
            return result
        }
    }

    override fun parseVariable(sender: Player, forward: Boolean, arg: String): Component? {
        return mirrorParse {
            val menu = buildMenu<Linked<ItemStack>>("${sender.name}'s Ender Chest") {
                rows(3)
                slots(inventorySlots)
                elements {
                    (0..26).map { sender.enderChest.getItem(it).replaceAir() }
                }
                onGenerate { _, element, _, _ ->
                    element
                }
            }
            val sha1 = Base64.getEncoder().encodeToString(sender.inventory.serializeToByteArray()).digest("sha-1")
            cache.put(sha1, menu)
            sender.getComponentFromLang("Function-EnderChest-Show-Format", sender.name, sha1)
        }
    }

    override fun canUse(sender: Player): Boolean {
        return sender.passPermission(permission)
    }

    private val inventorySlots = IntRange(0, 26).toList()

    private val AIR_ITEM = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = "§r" }

    private fun ItemStack?.replaceAir() = if (isAir()) AIR_ITEM else this!!
}