package me.arasple.mc.trchat.module.internal.command.main

import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.getSession
import me.arasple.mc.trchat.util.muteDateFormat
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.common.platform.function.onlinePlayers
import taboolib.common5.Demand
import taboolib.common5.util.parseMillis
import taboolib.expansion.createHelper
import taboolib.platform.util.sendLang

/**
 * CommandPrivateMessage
 * me.arasple.mc.trchat.module.internal.command
 *
 * @author wlys
 * @since 2021/7/21 10:40
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object CommandMute {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("mute", description = "Mute", permission = "trchat.command.mute") {
            dynamic("player") {
                suggestion<CommandSender>(uncheck = true) { _, _ ->
                    onlinePlayers().map { it.name }
                }
                execute<CommandSender> { sender, _, argument ->
                    val player = Bukkit.getPlayer(argument) ?: return@execute sender.sendLang("Command-Player-Not-Exist")
                    val session = player.getSession()
                    session.updateMuteTime("999d".parseMillis())
                    sender.sendLang("Mute-Muted-Player", player.name, "999d", "null")
                    player.sendLang("General-Muted", muteDateFormat.format(session.muteTime), session.muteReason)
                }
                dynamic("options", optional = true) {
                    suggestion<CommandSender>(uncheck = true) { _, _ ->
                        listOf("-t 1h", "-t 2d", "-t 15m", "-r 原因", "--cancel")
                    }
                    execute<CommandSender> { sender, context, argument ->
                        val player = Bukkit.getPlayer(context.argument(-1)) ?: return@execute sender.sendLang("Command-Player-Not-Exist")
                        val session = player.getSession()
                        val de = Demand("mute $argument")
                        if (de.tags.contains("cancel")) {
                            session.updateMuteTime(0)
                            sender.sendLang("Mute-Cancel-Muted-Player", player.name)
                            player.sendLang("General-Cancel-Muted")
                        } else {
                            val time = de.get("t") ?: "999d"
                            val reason = de.get("r")
                            session.updateMuteTime(time.parseMillis())
                            session.setMuteReason(reason)
                            sender.sendLang("Mute-Muted-Player", player.name, time, reason ?: "null")
                            player.sendLang("General-Muted", muteDateFormat.format(session.muteTime), session.muteReason)
                        }
                    }
                }
            }
            incorrectCommand { _, _, _, _ ->
                createHelper()
            }
        }
        command("muteall", listOf("globalmute"), "Mute all", permission = "trchat.command.muteall") {
            execute<CommandSender> { sender, _, _ ->
                TrChatBukkit.isGlobalMuting = !TrChatBukkit.isGlobalMuting
                if (TrChatBukkit.isGlobalMuting) {
                    sender.sendLang("Mute-Muted-All")
                } else {
                    sender.sendLang("Mute-Cancel-Muted-All")
                }
            }
        }
    }
}