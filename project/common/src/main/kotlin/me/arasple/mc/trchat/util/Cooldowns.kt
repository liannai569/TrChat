package me.arasple.mc.trchat.util

import com.google.common.collect.Maps
import java.util.*

/**
 * @author wlys
 * @since 2022/3/5 14:09
 */
class Cooldowns {

    val data = Maps.newConcurrentMap<String, Long>()!!

    companion object {

        private val COOLDOWNS = mutableMapOf<UUID, Cooldowns>()

        fun getCooldownLeft(uuid: UUID, type: CooldownType): Long {
            return COOLDOWNS.computeIfAbsent(uuid) { Cooldowns() }.data[type.alias]?.let { it - System.currentTimeMillis() } ?: -1
        }

        fun isInCooldown(uuid: UUID, type: CooldownType): Boolean {
            return getCooldownLeft(uuid, type) > 0
        }

        fun updateCooldown(uuid: UUID, type: CooldownType, lasts: Long) {
            COOLDOWNS.computeIfAbsent(uuid) { Cooldowns() }.let { cooldowns ->
                cooldowns.data[type.alias] = System.currentTimeMillis() + lasts
            }
        }

    }
}

enum class CooldownType(val alias: String) {

    /**
     * Chat Cooldown Types
     */

    CHAT("Chat"),
    ITEM_SHOW("ItemShow"),
    MENTION("Mention"),
    INVENTORY_SHOW("InventoryShow"),
    ENDERCHEST_SHOW("EnderChestShow")

}