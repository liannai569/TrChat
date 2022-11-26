package me.arasple.mc.trchat.module.internal.proxy

import me.arasple.mc.trchat.module.display.function.standard.Mention
import me.arasple.mc.trchat.module.internal.data.PlayerData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.onlinePlayers
import taboolib.module.chat.uncolored

/**
 * @author Arasple
 * @date 2019/8/4 21:28
 */
@PlatformSide([Platform.BUKKIT])
object BukkitPlayers {

    private var players = listOf<String>()

    fun getRegex(player: Player): Regex? {
        val names = getPlayers()
            .filter { (Mention.selfMention || it != player.name) && !PlayerData.vanishing.contains(it) }
            .takeIf { it.isNotEmpty() }
            ?.joinToString("|") { Regex.escape(it) }
            ?: return null
        return Regex("@? ?($names)", RegexOption.IGNORE_CASE)
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
        val result = mutableSetOf<String>()
        result += players
        result += onlinePlayers().map { it.displayName?.uncolored() ?: it.name }
        return result.filter { it.isNotBlank() }
    }

    fun setPlayers(players: List<String>) {
        BukkitPlayers.players = players
    }

}