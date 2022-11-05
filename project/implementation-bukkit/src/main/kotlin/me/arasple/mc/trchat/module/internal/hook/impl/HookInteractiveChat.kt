package me.arasple.mc.trchat.module.internal.hook.impl

import com.loohp.interactivechat.api.InteractiveChatAPI
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer
import me.arasple.mc.trchat.module.internal.hook.HookAbstract
import me.arasple.mc.trchat.util.gson
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author wlys
 * @since 2022/3/19 14:17
 */
class HookInteractiveChat : HookAbstract() {

    fun sendMessage(receiver: CommandSender, component: Component): Boolean {
        return if (isHooked) {
            InteractiveChatAPI.sendMessage(receiver, InteractiveChatComponentSerializer.gson().deserialize(gson(component)))
            true
        } else {
            false
        }
    }

    fun createItemDisplayComponent(player: Player, item: ItemStack): Component? {
        return if (isHooked) {
            gson(InteractiveChatComponentSerializer.gson().serialize(InteractiveChatAPI.createItemDisplayComponent(player, item)))
        } else {
            null
        }
    }
}