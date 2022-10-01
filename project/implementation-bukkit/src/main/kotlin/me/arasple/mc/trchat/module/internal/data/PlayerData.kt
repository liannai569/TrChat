package me.arasple.mc.trchat.module.internal.data

import me.arasple.mc.trchat.util.getDataContainer
import me.arasple.mc.trchat.util.toUUID
import org.bukkit.OfflinePlayer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author wlys
 * @since 2022/6/25 18:17
 */
class PlayerData(val player: OfflinePlayer) {

    init {
        if (isVanishing) {
            vanishing += player.name!!
        }
    }

    val isSpying get() = player.getDataContainer().getBoolean("spying", false)

    val isFilterEnabled get() = player.getDataContainer().getBoolean("filter", true)

    val muteTime get() = player.getDataContainer().getLong("mute_time", 0)

    val isMuted get() = muteTime > System.currentTimeMillis()

    val muteReason get() = player.getDataContainer().getString("mute_reason", "null")!!

    val isVanishing get() = player.getDataContainer().getBoolean("vanish", false)

    val ignored get() = player.getDataContainer().getStringList("ignored").map { it.toUUID() }

    fun selectColor(color: String?) {
        player.getDataContainer()["color"] = color
    }

    fun setFilter(value: Boolean) {
        player.getDataContainer()["filter"] = value
    }

    fun updateMuteTime(time: Long) {
        player.getDataContainer()["mute_time"] = System.currentTimeMillis() + time
    }

    fun setMuteReason(reason: String?) {
        player.getDataContainer()["mute_reason"] = reason
    }

    fun switchSpy(): Boolean {
        player.getDataContainer()["spying"] = !isSpying
        return isSpying
    }

    fun switchVanish(): Boolean {
        player.getDataContainer()["vanish"] = !isVanishing
        return isVanishing.also {
            if (it) vanishing += player.name!! else vanishing -= player.name!!
        }
    }

    fun addIgnored(uuid: UUID) {
        player.getDataContainer()["ignored"] = player.getDataContainer().getStringList("ignored") + uuid.toString()
    }

    fun removeIgnored(uuid: UUID) {
        player.getDataContainer()["ignored"] = player.getDataContainer().getStringList("ignored") - uuid.toString()
    }

    fun switchIgnored(uuid: UUID): Boolean {
        return if (ignored.contains(uuid)) {
            removeIgnored(uuid)
            false
        } else {
            addIgnored(uuid)
            true
        }
    }

    companion object {

        @JvmField
        val DATA = ConcurrentHashMap<UUID, PlayerData>()

        val vanishing = mutableSetOf<String>()

        fun getData(player: OfflinePlayer): PlayerData {
            return DATA.computeIfAbsent(player.uniqueId) {
                PlayerData(player)
            }
        }

    }
}