package me.arasple.mc.trchat.module.internal.hook.ext

import me.arasple.mc.trchat.module.internal.script.js.JavaScriptAgent
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.getSession
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
            val session = player.getSession()

            return when (params[0].lowercase()) {
                "js" -> if (params.size > 1) JavaScriptAgent.eval(player, args.substringAfter('_')).get() else ""
                "filter" -> session.isFilterEnabled
                "channel" -> session.channel?.id
                "toplayer" -> session.lastPrivateTo
                "spy" -> session.isSpying
                "lastmessage" -> session.lastMessage
                else -> ""
            }.toString()
        }

        return "__"
    }
}