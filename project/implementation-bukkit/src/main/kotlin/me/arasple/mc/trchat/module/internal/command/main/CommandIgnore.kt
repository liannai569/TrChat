package me.arasple.mc.trchat.module.internal.command.main

import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.data
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.common.platform.command.suggestPlayers
import taboolib.expansion.createHelper
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * @author wlys
 * @since 2022/2/6 15:01
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object CommandIgnore {

    @Suppress("Deprecation")
    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("ignore", permission = "trchat.command.ignore") {
            dynamic("player") {
                suggestPlayers()
                execute<Player> { sender, _, argument ->
                    val player = Bukkit.getOfflinePlayer(argument)
                    if (!player.hasPlayedBefore()) {
                        return@execute sender.sendLang("Command-Player-Not-Exist")
                    }
                    sender.data.switchIgnored(player.uniqueId)
                }
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
            incorrectCommand { _, _, _, _ ->
                createHelper()
            }
        }
    }
}