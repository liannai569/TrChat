package me.arasple.mc.trchat.module.internal.command

import me.arasple.mc.trchat.api.config.Filters
import me.arasple.mc.trchat.api.config.Functions
import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.module.conf.Loader
import me.arasple.mc.trchat.module.display.menu.MenuControlPanel
import me.arasple.mc.trchat.module.display.menu.MenuFilterControl
import me.arasple.mc.trchat.module.internal.command.sub.CommandRemoveMessage
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.getSession
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.common5.Mirror
import taboolib.expansion.createHelper
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * CommandHandler
 * me.arasple.mc.trchat.module.internal.command
 *
 * @author wlys
 * @since 2021/8/21 12:23
 */
@Internal
@PlatformSide([Platform.BUKKIT])
@CommandHeader("trchat", ["trc"], "TrChat主命令", permission = "trchat.access")
object CommandHandler {

    @CommandBody(permission = "trchat.command.reload", optional = true)
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            Settings.CONF.reload()
            Functions.CONF.reload()
            Filters.CONF.reload()
            Loader.loadChannels(sender)
        }
    }

    @CommandBody(permission = "trchat.command.controlpanel", optional = true)
    val controlPanel = subCommand {
        execute<Player> { sender, _, _ ->
            MenuControlPanel.displayFor(sender)
        }
    }

    @CommandBody(permission = "trchat.command.chatfilter", optional = true)
    val chatFilter = subCommand {
        execute<Player> { sender, _, _ ->
            MenuFilterControl.displayFor(sender)
        }
    }

    @CommandBody(permission = "trchat.command.mirror", optional = true)
    val mirror = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            submit(async = true) {
                sender.sendMessage("\n§b§lTrChat §a§l§nPerformance Mirror\n§r")
                Mirror.report(sender) {
                    childFormat = "§8  {0}§7{1} §2[{3} ms] §7{4}%"
                    parentFormat = "§8  §8{0}§7{1} §8[{3} ms] §7{4}%"
                }
            }
        }
    }

    @CommandBody(permission = "trchat.command.spy", optional = true)
    val spy = subCommand {
        execute<Player> { sender, _, _ ->
            val state = sender.getSession().switchSpy()
            sender.sendLang(if (state) "Private-Message-Spy-On" else "Private-Message-Spy-Off")
        }
    }

    @CommandBody(permission = "trchat.command.removemessage", optional = true)
    val removeMessage = CommandRemoveMessage.command

    @CommandBody(permission = "trchat.command.vanish", optional = true)
    val vanish = subCommand {
        execute<Player> { sender, _, _ ->
            sender.getSession().switchVanish()
        }
    }

    @CommandBody
    val help = subCommand {
        createHelper()
    }

    @CommandBody
    val main = mainCommand {
        createHelper()
        incorrectSender { sender, _ ->
            sender.sendLang("Command-Not-Player")
        }
        incorrectCommand { _, _, _, _ ->
            createHelper()
        }
    }
}