package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.display.function.EnderChestShow
import me.arasple.mc.trchat.module.display.function.InventoryShow
import me.arasple.mc.trchat.module.display.function.ItemShow
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.util.*
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.Strings
import taboolib.platform.util.sendLang

/**
 * @author Arasple, wlys
 * @date 2019/11/30 12:10
 */
@PlatformSide([Platform.BUKKIT])
object ListenerChatEvent {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(e: AsyncPlayerChatEvent) {
        e.isCancelled = true
        val player = e.player
        var message = e.message
        val session = player.getSession()

        session.recipients = e.recipients

        if (!player.checkMute()) {
            return
        }

        message = HookPlugin.getItemsAdder().replaceFontImages(player, message)

        if (!checkLimits(player, message)) {
            return
        }

        e.handlers.registeredListeners
            .filter {
//                it.plugin.isEnabled
//                    && (it.priority == org.bukkit.event.EventPriority.MONITOR
//                    && it.isIgnoringCancelled) || hooks.contains(it.plugin.name)
                Settings.CONF.getStringList("Options.ChatEvent-Hooks").contains(it.plugin.name)
            }.forEach {
                try {
                    it.callEvent(AsyncPlayerChatEvent(e.isAsynchronous, e.player, e.message, e.recipients))
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

        Channel.channels.forEach { channel ->
            channel.bindings.prefix?.forEach {
                if (message.startsWith(it, ignoreCase = true)) {
                    channel.execute(player, message.substring(it.length))
                    return
                }
            }
        }

        session.channel?.execute(player, message)
    }

    private fun checkLimits(player: Player, message: String): Boolean {
        if (player.hasPermission("trchat.bypass.*")) {
            return true
        }
        if (!player.hasPermission("trchat.bypass.chatlength")) {
            if (message.length > Settings.chatLengthLimit) {
                player.sendLang("General-Too-Long", message.length, Settings.chatLengthLimit)
                return false
            }
        }
        if (!player.hasPermission("trchat.bypass.repeat")) {
            val lastMessage = player.getSession().lastMessage
            if (Strings.similarDegree(lastMessage, message) > Settings.chatSimilarity) {
                player.sendLang("General-Too-Similar")
                return false
            }
        }
        if (!player.hasPermission("trchat.bypass.chatcd")) {
            val chatCooldown = player.getCooldownLeft(CooldownType.CHAT)
            if (chatCooldown > 0) {
                player.sendLang("Cooldowns-Chat", chatCooldown / 1000)
                return false
            }
        }
        if (!player.hasPermission("trchat.bypass.itemcd")) {
            val itemCooldown = player.getCooldownLeft(CooldownType.ITEM_SHOW)
            if (ItemShow.keys.any { message.contains(it, ignoreCase = true) } && itemCooldown > 0) {
                player.sendLang("Cooldowns-Item-Show", itemCooldown / 1000)
                return false
            } else {
                player.updateCooldown(CooldownType.ITEM_SHOW, ItemShow.cooldown.get())
            }
        }
        if (!player.hasPermission("trchat.bypass.inventorycd")) {
            val inventoryCooldown = player.getCooldownLeft(CooldownType.INVENTORY_SHOW)
            if (InventoryShow.keys.any { message.contains(it, ignoreCase = true) } && inventoryCooldown > 0) {
                player.sendLang("Cooldowns-Inventory-Show", inventoryCooldown / 1000)
                return false
            } else {
                player.updateCooldown(CooldownType.INVENTORY_SHOW, InventoryShow.cooldown.get())
            }
        }
        if (!player.hasPermission("trchat.bypass.enderchestcd")) {
            val enderchestCooldown = player.getCooldownLeft(CooldownType.ENDERCHEST_SHOW)
            if (EnderChestShow.keys.any { message.contains(it, ignoreCase = true) } && enderchestCooldown > 0) {
                player.sendLang("Cooldowns-EnderChest-Show", enderchestCooldown / 1000)
                return false
            } else {
                player.updateCooldown(CooldownType.ENDERCHEST_SHOW, EnderChestShow.cooldown.get())
            }
        }
        player.updateCooldown(CooldownType.CHAT, Settings.chatCooldown.get())
        return true
    }
}