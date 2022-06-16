package me.arasple.mc.trchat.module.internal.proxy.bukkit

import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.display.function.Mention
import me.arasple.mc.trchat.module.internal.proxy.Proxy
import me.arasple.mc.trchat.module.internal.proxy.bungee.Bungees
import me.arasple.mc.trchat.util.Internal
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit

/**
 * @author Arasple
 * @date 2019/8/4 21:28
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object Players {

    private var players = listOf<String>()

    fun getRegex(player: Player): List<Regex> {
        return getPlayers().filter { (Mention.selfMention || it != player.name) && !ChatSession.vanishing.contains(it) }.map {
            Regex("(?i)@? ?($it)")
        }
    }

    @Schedule(delay = 20)
    fun sendPlayerList() {
        if (Proxy.isEnabled && Proxy.platform == Platform.BUNGEE) {
            submit(period = 60, async = true) {
                if (Bukkit.getOnlinePlayers().isNotEmpty()) {
                    Bungees.sendBungeeData(Bukkit.getOnlinePlayers().iterator().next(), "PlayerList", "ALL")
                }
            }
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
        players += Players.players
        players += onlinePlayers().map { it.name }
        return players.filter { it.isNotBlank() }
    }

    fun setPlayers(players: List<String>) {
        Players.players = players
    }
}