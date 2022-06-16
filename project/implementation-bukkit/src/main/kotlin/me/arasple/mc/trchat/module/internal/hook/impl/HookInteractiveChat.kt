package me.arasple.mc.trchat.module.internal.hook.impl

import me.arasple.mc.trchat.module.internal.hook.HookAbstract
import me.arasple.mc.trchat.util.Internal
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author wlys
 * @since 2022/3/19 14:17
 */
@Internal
class HookInteractiveChat : HookAbstract() {

    fun sendMessage(receiver: CommandSender, component: Component): Boolean {
//         return if (isHooked) {
//             InteractiveChatAPI.sendMessage(receiver, component as com.loohp.interactivechat.libs.net.kyori.adventure.text.Component)
//            true
//        } else {
//            false
//         }
        return false
    }

    fun createItemDisplayComponent(player: Player, item: ItemStack): Component? {
//        return if (isHooked) {
//            InteractiveChatAPI.createItemDisplayComponent(player, item) as Component
//        } else {
//            null
//        }
        return null
    }
}