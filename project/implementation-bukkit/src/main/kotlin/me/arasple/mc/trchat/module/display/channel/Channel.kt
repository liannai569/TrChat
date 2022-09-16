package me.arasple.mc.trchat.module.display.channel

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.display.channel.obj.ChannelBindings
import me.arasple.mc.trchat.module.display.channel.obj.ChannelEvents
import me.arasple.mc.trchat.module.display.channel.obj.ChannelSettings
import me.arasple.mc.trchat.module.display.channel.obj.Target
import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.internal.data.ChatLogs
import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyManager
import me.arasple.mc.trchat.module.internal.service.Metrics
import me.arasple.mc.trchat.util.*
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.command
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.unregisterCommand
import taboolib.common.util.subList
import taboolib.module.lang.sendLang
import taboolib.platform.util.onlinePlayers
import taboolib.platform.util.sendLang

/**
 * @author wlys
 * @since 2021/12/11 22:27
 */
open class Channel(
    val id: String,
    val settings: ChannelSettings,
    val bindings: ChannelBindings,
    val events: ChannelEvents,
    val formats: List<Format>,
    val console: Format? = null,
    val listeners: MutableSet<String> = mutableSetOf()
) {

    init {
        if (settings.autoJoin) {
           onlinePlayers.forEach {
                if (settings.joinPermission == null || it.hasPermission(settings.joinPermission)) {
                    listeners.add(it.name)
                }
            }
        } else {
            onlinePlayers.filter { it.session.channel == id }.forEach {
                join(it, id, hint = false)
            }
        }
        initCommand()
    }

    private fun initCommand() {
        if (bindings.command.isNullOrEmpty()) {
            return
        }
        command(bindings.command[0], subList(bindings.command, 1), "Channel $id speak command", permission = settings.joinPermission ?: "") {
            execute<Player> { sender, _, _ ->
                if (sender.session.channel == this@Channel.id) {
                    quit(sender)
                } else {
                    join(sender, this@Channel)
                }
            }
            dynamic("message", optional = true) {
                execute<CommandSender> { sender, _, argument ->
                    if (sender is Player) {
                        execute(sender, argument)
                    } else {
                        execute(sender, argument)
                    }
                }
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
        }
    }

    open fun execute(sender: CommandSender, message: String) {
        if (sender is Player) {
            execute(sender, message)
            return
        }
        val builder = Component.text()
        console?.let { format ->
            format.prefix.forEach { prefix ->
                builder.append(prefix.value[0].content.toTextComponent(sender)) }
            builder.append(format.msg.createComponent(sender, message, settings.disabledFunctions, true))
            format.suffix.forEach { suffix ->
                builder.append(suffix.value[0].content.toTextComponent(sender)) }
        } ?: return
        val component = builder.build()

        if (settings.proxy && BukkitProxyManager.processor != null) {
            val gson = gson(component)
            if (settings.ports != null) {
                Bukkit.getServer().sendTrChatMessage(
                    "ForwardRaw",
                    Identity.nil().uuid().parseString(),
                    gson,
                    settings.joinPermission ?: "null",
                    settings.ports.joinToString(";"),
                    settings.doubleTransfer.toString()
                )
            } else {
                Bukkit.getServer().sendTrChatMessage(
                    "BroadcastRaw",
                    Identity.nil().uuid().parseString(),
                    gson,
                    settings.joinPermission ?: "null",
                    settings.doubleTransfer.toString()
                )
            }
        } else {
            listeners.forEach {
                getProxyPlayer(it)?.sendComponent(it, component)
            }
            sender.sendComponent(null, component)
        }
    }

    open fun execute(player: Player, message: String, forward: Boolean = true): Pair<Component, Component?>? {
        if (!player.checkMute()) {
            return null
        }
        if (!settings.speakCondition.pass(player)) {
            player.sendLang("Channel-No-Speak-Permission")
            return null
        }
        if (settings.filterBeforeSending && TrChat.api().getFilterManager().filter(message).sensitiveWords > 0) {
            player.sendLang("Channel-Bad-Language")
            return null
        }
        val event = TrChatEvent(this, player.session, message, forward)
        if (!event.call()) {
            return null
        }
        val msg = events.process(player, event.message) ?: return null

        val builder = Component.text()
        formats.firstOrNull { it.condition.pass(player) }?.let { format ->
            format.prefix
                .mapNotNull { prefix -> prefix.value.firstOrNull { it.condition.pass(player) }?.content?.toTextComponent(player) }
                .forEach { prefix -> builder.append(prefix) }
            builder.append(format.msg.createComponent(player, msg, settings.disabledFunctions, forward))
            format.suffix
                .mapNotNull { suffix -> suffix.value.firstOrNull { it.condition.pass(player) }?.content?.toTextComponent(player) }
                .forEach { suffix -> builder.append(suffix) }
        } ?: return null
        val component = builder.build()

        if (!forward) {
            return component to null
        }

        // TODO: 跨服事件传递
        if (settings.proxy && BukkitProxyManager.processor != null) {
            val gson = gson(component)
            if (settings.ports != null) {
                player.sendTrChatMessage(
                    "ForwardRaw",
                    player.uniqueId.parseString(),
                    gson,
                    settings.joinPermission ?: "null",
                    settings.ports.joinToString(";"),
                    settings.doubleTransfer.toString()
                )
            } else {
                player.sendTrChatMessage(
                    "BroadcastRaw",
                    player.uniqueId.parseString(),
                    gson,
                    settings.joinPermission ?: "null",
                    settings.doubleTransfer.toString()
                )
            }
            return component to null
        }
        when (settings.target.range) {
            Target.Range.ALL -> {
                listeners.filter { events.send(player, it, msg) }.forEach {
                    getProxyPlayer(it)?.sendComponent(player, component)
                }
            }
            Target.Range.SINGLE_WORLD -> {
                onlinePlayers.filter { listeners.contains(it.name)
                        && it.world == player.world
                        && events.send(player, it.name, msg) }.forEach {
                    it.sendComponent(player, component)
                }
            }
            Target.Range.DISTANCE -> {
                onlinePlayers.filter { listeners.contains(it.name)
                        && it.world == player.world
                        && it.location.distance(player.location) <= settings.target.distance
                        && events.send(player, it.name, msg) }.forEach {
                    it.sendComponent(player, component)
                }
            }
            Target.Range.SELF -> {
                if (events.send(player, player.name, msg)) {
                    player.sendComponent(player, component)
                }
            }
        }
        console().cast<CommandSender>().sendComponent(player, component)

        player.session.lastMessage = msg
        ChatLogs.log(player, msg)
        Metrics.increase(0)

        return component to null
    }

    open fun unregister() {
        bindings.command?.forEach { unregisterCommand(it) }
        listeners.clear()
    }

    companion object {

        val channels = mutableMapOf<String, Channel>()

        fun join(player: Player, channel: String, hint: Boolean = true): Boolean {
            channels[channel]?.let {
                return join(player, it, hint)
            }
            quit(player)
            return false
        }

        fun join(player: Player, channel: Channel, hint: Boolean = true): Boolean {
            if (channel.settings.joinPermission?.let { player.hasPermission(it) } == false) {
                player.sendLang("General-No-Permission")
                return false
            }
            player.session.channel = channel.id
            channel.listeners.add(player.name)
            channel.events.join(player)

            if (hint) {
                player.sendLang("Channel-Join", channel.id)
            }
            return true
        }

        fun quit(player: Player) {
            player.session.getChannel()?.let {
                if (!it.settings.autoJoin) {
                    it.listeners.remove(player.name)
                }
                it.events.quit(player)
                player.sendLang("Channel-Quit", it.id)
            }
            if (!join(player, Settings.defaultChannel)) {
                player.session.channel = null
            }
        }
    }
}