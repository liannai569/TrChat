@file:Suppress("Deprecation")

package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.display.function.standard.*
import me.arasple.mc.trchat.util.*
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.Strings
import taboolib.platform.util.sendLang

/**
 * @author ItsFlicker
 * @date 2019/11/30 12:10
 */
@PlatformSide([Platform.BUKKIT])
object ListenerBukkitChat {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(e: AsyncPlayerChatEvent) {
        if (e.isCancelled) return
        e.recipients.clear()
        val player = e.player
        val session = player.session

        if (!player.checkMute()) {
            return
        }
        if (!checkLimits(player, e.message)) {
            return
        }

        Channel.channels.values.forEach { channel ->
            channel.bindings.prefix?.forEach {
                if (e.message.startsWith(it, ignoreCase = true)) {
                    channel.execute(player, e.message.substring(it.length))
                    return
                }
            }
        }

        session.getChannel()?.execute(player, e.message)
    }

    private fun checkLimits(player: Player, message: String): Boolean {
        if (player.hasPermission("trchat.bypass.*")) {
            return true
        }
        if (!player.hasPermission("trchat.bypass.chatlength")) {
            if (message.length > Settings.chatLengthLimit) {
                player.sendLang("General-Too-Long", message.length, Settings.chatLengthLimit)
                return false
            }
        }
        if (!player.hasPermission("trchat.bypass.repeat")) {
            val lastMessage = player.session.lastMessage
            if (Strings.similarDegree(lastMessage, message) > Settings.chatSimilarity) {
                player.sendLang("General-Too-Similar")
                return false
            }
        }
        if (!player.hasPermission("trchat.bypass.chatcd")) {
            val chatCooldown = player.getCooldownLeft(CooldownType.CHAT)
            if (chatCooldown > 0) {
                player.sendLang("Cooldowns-Chat", chatCooldown / 1000)
                return false
            }
        }
        if (Function.functions.any { !it.checkCooldown(player, message) }) {
            return false
        }
        player.updateCooldown(CooldownType.CHAT, Settings.chatCooldown.get())
        return true
    }

}