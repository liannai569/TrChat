package me.arasple.mc.trchat.module.display.function.standard

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.arasple.mc.trchat.module.conf.file.Functions
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.display.function.StandardFunction
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.module.internal.script.Reaction
import me.arasple.mc.trchat.util.hoverItemFixed
import me.arasple.mc.trchat.util.legacy
import me.arasple.mc.trchat.util.passPermission
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.asList
import taboolib.common.util.replaceWithOrder
import taboolib.common.util.resettableLazy
import taboolib.common5.util.parseMillis
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.getI18nName
import taboolib.module.nms.getInternalName
import taboolib.platform.util.buildItem

/**
 * @author wlys
 * @since 2022/3/12 19:14
 */
@StandardFunction
@PlatformSide([Platform.BUKKIT])
object ItemShow : Function("ITEM") {

    override val alias = "Item-Show"

    override val reaction by resettableLazy("functions") {
        Functions.CONF["General.Item-Show.Action"]?.let { Reaction(it.asList()) }
    }

    @ConfigNode("General.Item-Show.Enabled", "function.yml")
    var enabled = true

    @ConfigNode("General.Item-Show.Permission", "function.yml")
    var permission = "none"

    @ConfigNode("General.Item-Show.Compatible", "function.yml")
    var compatible = false

    @ConfigNode("General.Item-Show.Origin-Name", "function.yml")
    var originName = false

    @ConfigNode("General.Item-Show.Type", "function.yml")
    var type = ConfigNodeTransfer<String, Type> { kotlin.runCatching { Type.valueOf(this.uppercase()) }.getOrDefault(Type.NAME) }

    @ConfigNode("General.Item-Show.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("General.Item-Show.Keys", "function.yml")
    var keys = emptyList<String>()

    private val cache: Cache<ItemStack, Component> = CacheBuilder.newBuilder()
        .maximumSize(50)
        .build()

    override fun createVariable(sender: Player, message: String): String {
        return if (!enabled) {
            message
        } else {
            var result = message
            keys.forEach { key ->
                (1..9).forEach {
                    result = result.replace("$key-$it", "{{ITEM:$it}}", ignoreCase = true)
                    result = result.replace("$key$it", "{{ITEM:$it}}", ignoreCase = true)
                }
                result = result.replace(key, "{{ITEM:${sender.inventory.heldItemSlot + 1}}}", ignoreCase = true)
            }
            result
        }
    }

    override fun parseVariable(sender: Player, forward: Boolean, arg: String): Component? {
        return mirrorParse {
            val item = (sender.inventory.getItem(arg.toInt() - 1) ?: ItemStack(Material.AIR)).let {
                if (compatible) {
                    buildItem(it) { material = Material.STONE }
                } else {
                    it.clone()
                }
            }
            cache.get(item) {
                if (HookPlugin.getInteractiveChat().isHooked) {
                    HookPlugin.getInteractiveChat().createItemDisplayComponent(sender, item)
                } else if (type.get() == Type.NAME || MinecraftVersion.major < 7) {
                    sender
                        .getComponentFromLang("Function-Item-Show-Format", item.getDisplayName(sender), item.amount)
                        ?.hoverItemFixed(item, sender)
                } else {
                    sender
                        .getComponentFromLang("Function-Item-Show-Format-Translatable", item.amount) { type, i, part, sender ->
                            val component = if (i == 1) {
                                Component.translatable(item.getInternalName())
                            } else {
                                legacy(
                                    part.text
                                        .replace("\\[", "[").replace("\\]", "]")
                                        .translate(sender).replaceWithOrder(item.amount)
                                )
                            }
                            component.toBuilder().applyStyle(type, part, 0, sender, item.amount)
                        }
                        ?.hoverItemFixed(item, sender)
                }
            }
        }
    }

    override fun canUse(sender: Player): Boolean {
        return sender.passPermission(permission)
    }

    @Suppress("Deprecation")
    private fun ItemStack.getDisplayName(player: Player): String {
        return if (originName || itemMeta?.hasDisplayName() != true) {
            getI18nName(player)
        } else {
            itemMeta!!.displayName
        }
    }

    enum class Type { NAME, TRANSLATABLE }

}