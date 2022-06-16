package me.arasple.mc.trchat.module.internal.command.main

import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.util.Internal
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.expansion.createHelper
import taboolib.module.lang.sendLang

/**
 * @author wlys
 * @since 2021/7/21 11:24
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object CommandChannel {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("channel", listOf("chatchannel", "trchannel"), "聊天频道", permission = "trchat.channel") {
            literal("join") {
                dynamic("channel") {
                    suggestion<Player> { _, _ ->
                        Channel.channels.map { it.id }
                    }
                    execute<Player> { sender, _, argument ->
                        Channel.join(sender, argument)
                    }
                }
            }
            literal("quit", "leave") {
                execute<Player> { sender, _, _ ->
                    Channel.quit(sender)
                }
            }
            execute<Player> { _, _, _ ->
                createHelper()
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