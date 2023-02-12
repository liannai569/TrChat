package me.arasple.mc.trchat.module.display.function.standard

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.arasple.mc.trchat.module.conf.file.Functions
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.display.function.StandardFunction
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.module.internal.hook.type.HookDisplayItem
import me.arasple.mc.trchat.module.internal.script.Reaction
import me.arasple.mc.trchat.util.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.asList
import taboolib.common.util.replaceWithOrder
import taboolib.common.util.resettableLazy
import taboolib.common5.util.parseMillis
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.chat.impl.DefaultComponent
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.getI18nName
import taboolib.module.nms.getInternalName
import taboolib.platform.util.buildItem
import taboolib.platform.util.sendLang

/**
 * @author ItsFlicker
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

    @ConfigNode("General.Item-Show.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("General.Item-Show.Keys", "function.yml")
    var keys = emptyList<String>()

    private val cache: Cache<ItemStack, ComponentText> = CacheBuilder.newBuilder()
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

    override fun parseVariable(sender: Player, forward: Boolean, arg: String): ComponentText? {
        return mirrorParse {
            val item = (sender.inventory.getItem(arg.toInt() - 1) ?: ItemStack(Material.AIR)).let {
                if (compatible) {
                    buildItem(it) { material = Material.STONE }
                } else {
                    var newItem = it.clone()
                    HookPlugin.registry.filterIsInstance(HookDisplayItem::class.java).forEach { element ->
                        newItem = element.displayItem(newItem, sender)
                    }
                    newItem
                }
            }
            cache.get(item) {
                sender
                    .getComponentFromLang("Function-Item-Show-Format-New", item.amount) { type, i, part, proxySender ->
                        val component = if (part.isVariable && part.text == "item") {
                            item.getNameComponent(sender)
                        } else {
                            Components.text(part.text.translate(proxySender).replaceWithOrder(item.amount))
                        }
                        component.applyStyle(type, part, i, proxySender, item.amount).hoverItemFixed(item)
                    }
            }
        }
    }

    override fun canUse(sender: Player): Boolean {
        return sender.passPermission(permission)
    }

    override fun checkCooldown(sender: Player, message: String): Boolean {
        if (enabled && keys.any { message.contains(it, ignoreCase = true) } && !sender.hasPermission("trchat.bypass.itemcd")) {
            val itemCooldown = sender.getCooldownLeft(CooldownType.ITEM_SHOW)
            if (itemCooldown > 0) {
                sender.sendLang("Cooldowns-Item-Show", itemCooldown / 1000)
                return false
            } else {
                sender.updateCooldown(CooldownType.ITEM_SHOW, cooldown.get())
            }
        }
        return true
    }

    @Suppress("Deprecation")
    private fun ItemStack.getNameComponent(player: Player): ComponentText {
        return if (originName || itemMeta?.hasDisplayName() != true) {
            if (MinecraftVersion.major >= 7) {
                Components.empty().appendTranslation(getInternalName())
            } else {
                Components.text(getI18nName(player))
            }
        } else {
            try {
                Components.empty().append(DefaultComponent(itemMeta!!.displayNameComponent.toList()))
            } catch (_: Throwable) {
                Components.text(itemMeta!!.displayName)
            }
        }
    }

}