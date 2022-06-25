package me.arasple.mc.trchat.module.internal.hook.ext

import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.data
import me.arasple.mc.trchat.util.session
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.platform.compat.PlaceholderExpansion

/**
 * TrChatPlaceholders
 * me.arasple.mc.trchat.module.internal.hook
 *
 * @author Arasple
 * @since 2021/8/9 23:09
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object HookPlaceholderAPI : PlaceholderExpansion {

    override val identifier: String
        get() = "trchat"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (player != null && player.isOnline) {
            val params = args.split('_')
            val session = player.session
            val data = player.data

            return when (params[0].lowercase()) {
//                "js" -> if (params.size > 1) JavaScriptAgent.eval(player, args.substringAfter('_')).get() else ""
                "channel" -> session.channel
                "toplayer" -> session.lastPrivateTo
                "lastmessage" -> session.lastMessage
                "spy" -> data.isSpying
                "filter" -> data.isFilterEnabled
                "mute" -> data.isMuted
                "vanish" -> data.isVanishing
                else -> "ERROR"
            }.toString()
        }
        return "ERROR"
    }

    override fun onPlaceholderRequest(player: OfflinePlayer?, args: String): String {
        if (player != null) {
            val params = args.split('_')
            val data = player.data

            return when (params[0].lowercase()) {
                "spy" -> data.isSpying
                "filter" -> data.isFilterEnabled
                "mute" -> data.isMuted
                "vanish" -> data.isVanishing
                else -> "ERROR"
            }.toString()
        }
        return "ERROR"
    }
}