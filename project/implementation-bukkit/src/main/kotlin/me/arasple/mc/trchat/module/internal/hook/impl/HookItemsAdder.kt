package me.arasple.mc.trchat.module.internal.hook.impl

import dev.lone.itemsadder.api.FontImages.FontImageWrapper
import me.arasple.mc.trchat.module.internal.hook.HookAbstract
import org.bukkit.entity.Player

/**
 * @author wlys
 * @since 2022/2/5 22:30
 */
class HookItemsAdder : HookAbstract() {

    fun replaceFontImages(player: Player, message: String): String {
        return if (isHooked) {
            FontImageWrapper.replaceFontImages(player, message)
        } else {
            message
        }
    }
}