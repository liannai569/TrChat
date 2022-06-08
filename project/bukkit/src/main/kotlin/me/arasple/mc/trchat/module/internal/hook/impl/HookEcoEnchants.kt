package me.arasple.mc.trchat.module.internal.hook.impl

import com.willfp.eco.core.display.Display
import me.arasple.mc.trchat.module.internal.hook.HookAbstract
import me.arasple.mc.trchat.util.Internal
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.isAir

/**
 * @author wlys
 * @since 2022/2/5 22:30
 */
@Internal
class HookEcoEnchants : HookAbstract() {

    fun displayItem(item: ItemStack, player: Player? = null): ItemStack {
        if (!isHooked || item.isAir()) {
            return item
        }
        return try {
            Display.displayAndFinalize(item, player)
        } catch (_: Throwable){
            Display.displayAndFinalize(item)
        }
    }
}