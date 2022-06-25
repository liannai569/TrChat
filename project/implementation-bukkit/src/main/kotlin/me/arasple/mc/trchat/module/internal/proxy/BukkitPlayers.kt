package me.arasple.mc.trchat.module.internal.proxy

import me.arasple.mc.trchat.module.display.function.Mention
import me.arasple.mc.trchat.module.internal.data.PlayerData
import me.arasple.mc.trchat.util.Internal
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.onlinePlayers

/**
 * @author Arasple
 * @date 2019/8/4 21:28
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object BukkitPlayers {

    private var players = listOf<String>()

    fun getRegex(player: Player): List<Regex> {
        return getPlayers().filter { (Mention.selfMention || it != player.name) && !PlayerData.vanishing.contains(it) }.map {
            Regex("(?i)@? ?($it)")
        }
    }

    fun isPlayerOnline(target: String): Boolean {
        val player = Bukkit.getPlayerExact(target)
        return player != null && player.isOnline || players.any { p -> p.equals(target, ignoreCase = true) }
    }

    fun getPlayerFullName(target: String): String? {
        val player = Bukkit.getPlayerExact(target)
        return if (player != null && player.isOnline) player.name else players.firstOrNull { it.equals(target, ignoreCase = true) }
    }

    fun getPlayers(): List<String> {
        val players = mutableSetOf<String>()
        players += BukkitPlayers.players
        players += onlinePlayers().map { it.name }
        return players.filter { it.isNotBlank() }
    }

    fun setPlayers(players: List<String>) {
        BukkitPlayers.players = players
    }
}