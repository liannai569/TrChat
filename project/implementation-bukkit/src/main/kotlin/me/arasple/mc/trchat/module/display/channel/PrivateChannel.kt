package me.arasple.mc.trchat.module.display.channel

import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.display.channel.obj.ChannelBindings
import me.arasple.mc.trchat.module.display.channel.obj.ChannelSettings
import me.arasple.mc.trchat.module.display.filter.ChatFilter
import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.internal.command.main.CommandReply
import me.arasple.mc.trchat.module.internal.data.ChatLogs
import me.arasple.mc.trchat.module.internal.proxy.Proxy
import me.arasple.mc.trchat.module.internal.proxy.bukkit.Players
import me.arasple.mc.trchat.module.internal.proxy.sendBukkitMessage
import me.arasple.mc.trchat.module.internal.proxy.sendProxyLang
import me.arasple.mc.trchat.module.internal.service.Metrics
import me.arasple.mc.trchat.util.*
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import taboolib.common.platform.command.command
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.util.subList
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * @author wlys
 * @since 2022/2/8 11:03
 */
class PrivateChannel(
    id: String,
    settings: ChannelSettings,
    bindings: ChannelBindings,
    val sender: List<Format>,
    val receiver: List<Format>
) : Channel(id, settings, bindings, emptyList()) {

    init {
        if (!bindings.command.isNullOrEmpty()) {
            command(bindings.command[0], subList(bindings.command, 1), "Channel $id speak command", permission = settings.joinPermission ?: "") {
                execute<Player> { sender, _, _ ->
                    if (sender.getSession().channel == this@PrivateChannel) {
                        quit(sender)
                    } else {
                        sender.sendLang("Private-Message-No-Player")
                    }
                }
                dynamic("player", optional = true) {
                    suggestion<Player> { _, _ ->
                        Players.getPlayers().filter { !ChatSession.vanishing.contains(it) }
                    }
                    execute<Player> { sender, _, argument ->
                        sender.getSession().lastPrivateTo = Players.getPlayerFullName(argument) ?: return@execute sender.sendLang("Command-Player-Not-Exist")
                        join(sender, this@PrivateChannel)
                    }
                    dynamic("message", optional = true) {
                        execute<Player> { sender, context, argument ->
                            Players.getPlayerFullName(context.argument(-1))?.let {
                                sender.getSession().lastPrivateTo = it
                                execute(sender, argument)
                            } ?: sender.sendLang("Command-Player-Not-Exist")
                        }
                    }
                }
                incorrectSender { sender, _ ->
                    sender.sendLang("Command-Not-Player")
                }
            }
        }
    }

    override fun execute(player: Player, message: String) {
        if (!player.checkMute()) {
            return
        }
        if (!settings.speakCondition.pass(player)) {
            player.sendLang("Channel-No-Speak-Permission")
            return
        }
        if (settings.filterBeforeSending && ChatFilter.filter(message).sensitiveWords > 0) {
            player.sendLang("Channel-Filter-Before-Sending")
            return
        }
        val session = player.getSession()
        val event = TrChatEvent(this, session, message)
        if (!event.call()) {
            return
        }

        val msg = event.message
        val builderSender = Component.text()
        sender.firstOrNull { it.condition.pass(player) }?.let { format ->
            format.prefix.forEach { prefix ->
                builderSender.append(prefix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
            builderSender.append(format.msg.serialize(player, msg, settings.disabledFunctions))
            format.suffix.forEach { suffix ->
                builderSender.append(suffix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
        } ?: return
        val send = builderSender.build()

        val builderReceiver = Component.text()
        receiver.firstOrNull { it.condition.pass(player) }?.let { format ->
            format.prefix.forEach { prefix ->
                builderReceiver.append(prefix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
            builderReceiver.append(format.msg.serialize(player, msg, settings.disabledFunctions))
            format.suffix.forEach { suffix ->
                builderReceiver.append(suffix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
        } ?: return
        val receive = builderReceiver.build()

        player.sendProcessedMessage(player, send)

        if (settings.proxy && Proxy.isEnabled) {
            player.sendBukkitMessage(
                "SendRaw",
                session.lastPrivateTo,
                gson(receive),
                settings.doubleTransfer.toString()
            )
            player.sendProxyLang("Private-Message-Receive", player.name)
        } else {
            getProxyPlayer(player.getSession().lastPrivateTo)?.let {
                it.sendProcessedMessage(player, receive)
                it.sendLang("Private-Message-Receive", player.name)
            }
        }

        ChatSession.SESSIONS.filterValues { it.isSpying }.entries.forEach { (_, v) ->
            v.player.sendLang("Private-Message-Spy-Format", player.name, session.lastPrivateTo, msg)
        }
        console().sendLang("Private-Message-Spy-Format", player.name, session.lastPrivateTo, msg)

        CommandReply.lastMessageFrom[session.lastPrivateTo] = player.name
        player.getSession().lastMessage = message
        ChatLogs.logPrivate(player.name, session.lastPrivateTo, message)
        Metrics.increase(0)
    }
}