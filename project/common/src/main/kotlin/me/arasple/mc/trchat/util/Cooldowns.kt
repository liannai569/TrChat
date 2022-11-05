package me.arasple.mc.trchat.util

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author wlys
 * @since 2022/3/5 14:09
 */
object Cooldowns {

    private val COOLDOWNS = ConcurrentHashMap<UUID, Cooldown>()

    fun getCooldownLeft(uuid: UUID, type: CooldownType): Long {
        return COOLDOWNS.computeIfAbsent(uuid) { Cooldown() }.data.getOrDefault(type.alias, 0L) - System.currentTimeMillis()
    }

    fun isInCooldown(uuid: UUID, type: CooldownType): Boolean {
        return getCooldownLeft(uuid, type) > 0
    }

    fun updateCooldown(uuid: UUID, type: CooldownType, lasts: Long) {
        COOLDOWNS.computeIfAbsent(uuid) { Cooldown() }.data[type.alias] = System.currentTimeMillis() + lasts
    }
}

data class Cooldown(val data: ConcurrentHashMap<String, Long> = ConcurrentHashMap())

enum class CooldownType(val alias: String) {

    /**
     * Chat Cooldown Types
     */

    CHAT("Chat"),
    ITEM_SHOW("ItemShow"),
    MENTION("Mention"),
    MENTION_ALL("MentionAll"),
    INVENTORY_SHOW("InventoryShow"),
    ENDERCHEST_SHOW("EnderChestShow"),
    IMAGE_SHOW("ImageShow")

}