package me.arasple.mc.trchat.module.internal.script.js

import org.bukkit.entity.Player
import taboolib.platform.compat.replacePlaceholder

/**
 * Assist
 * me.arasple.mc.trchat.internal.script.js
 *
 * @author wlys
 * @since 2021/8/27 16:44
 */
class Assist {

    companion object {

        val INSTANCE = Assist()
    }

    fun parsePlaceholders(player: Player, string: String): String {
        return string.replacePlaceholder(player)
    }
}