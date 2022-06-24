package me.arasple.mc.trchat.module.display.channel

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.module.display.channel.obj.ChannelBindings
import me.arasple.mc.trchat.module.display.channel.obj.ChannelEvents
import me.arasple.mc.trchat.module.display.channel.obj.ChannelSettings
import me.arasple.mc.trchat.module.display.channel.obj.Target
import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.internal.data.ChatLogs
import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyManager
import me.arasple.mc.trchat.module.internal.service.Metrics
import me.arasple.mc.trchat.util.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.command
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.unregisterCommand
import taboolib.common.util.subList
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import taboolib.platform.util.toProxyLocation
import java.util.*

/**
 * @author wlys
 * @since 2021/12/11 22:27
 */
open class Channel(
    val id: String,
    val settings: ChannelSettings,
    val bindings: ChannelBindings,
    val events: ChannelEvents?,
    val formats: List<Format>,
    val console: Format? = null,
    val listeners: MutableSet<UUID> = mutableSetOf()
) {

    init {
        if (settings.autoJoin) {
            Bukkit.getOnlinePlayers().forEach {
                if (settings.joinPermission == null || it.hasPermission(settings.joinPermission)) {
                    listeners.add(it.uniqueId)
                }
            }
        } else {
            Bukkit.getOnlinePlayers().filter { it.getSession().channel == id }.forEach {
                join(it, id, hint = false)
            }
        }
        if (!bindings.command.isNullOrEmpty()) {
            command(bindings.command[0], subList(bindings.command, 1), "Channel $id speak command", permission = settings.joinPermission ?: "") {
                execute<Player> { sender, _, _ ->
                    if (sender.getSession().channel == this@Channel.id) {
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
                            val builder = Component.text()
                            console?.let { format ->
                                format.prefix.forEach { prefix ->
                                    builder.append(prefix.value.first().content.toTextComponent(sender)) }
                                builder.append(format.msg.serialize(sender, argument, settings.disabledFunctions))
                                format.suffix.forEach { suffix ->
                                    builder.append(suffix.value.first().content.toTextComponent(sender)) }
                            } ?: return@execute
                            val component = builder.build()

                            if (settings.proxy && BukkitProxyManager.processor != null) {
                                val gson = gson(component)
                                if (settings.ports != null) {
                                    Bukkit.getServer().sendTrChatMessage(
                                        "ForwardRaw",
                                        UUID.randomUUID().toString(),
                                        gson,
                                        settings.joinPermission ?: "null",
                                        settings.ports.joinToString(";"),
                                        settings.doubleTransfer.toString()
                                    )
                                } else {
                                    Bukkit.getServer().sendTrChatMessage(
                                        "BroadcastRaw",
                                        UUID.randomUUID().toString(),
                                        gson,
                                        settings.joinPermission ?: "null",
                                        settings.doubleTransfer.toString()
                                    )
                                }
                                return@execute
                            }
                            listeners.forEach {
                                getProxyPlayer(it)?.sendComponent(it, component)
                            }
                            sender.sendComponent(UUID.randomUUID(), component)
                        }
                    }
                }
                incorrectSender { sender, _ ->
                    sender.sendLang("Command-Not-Player")
                }
            }
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
        if (settings.filterBeforeSending && TrChat.api().filter(message).sensitiveWords > 0) {
            player.sendLang("Channel-Bad-Language")
            return null
        }
        val event = TrChatEvent(this, player.getSession(), message, !forward)
        if (!event.call()) {
            return null
        }
        val msg = event.message

        val builder = Component.text()
        formats.firstOrNull { it.condition.pass(player) }?.let { format ->
            format.prefix.forEach { prefix ->
                builder.append(prefix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
            builder.append(format.msg.serialize(player, msg, settings.disabledFunctions))
            format.suffix.forEach { suffix ->
                builder.append(suffix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
        } ?: return null
        val component = builder.build()

        if (!forward) {
            return component to null
        }

        if (settings.proxy && BukkitProxyManager.processor != null) {
            val gson = gson(component)
            if (settings.ports != null) {
                player.sendTrChatMessage(
                    "ForwardRaw",
                    player.uniqueId.toString(),
                    gson,
                    settings.joinPermission ?: "null",
                    settings.ports.joinToString(";"),
                    settings.doubleTransfer.toString()
                )
            } else {
                player.sendTrChatMessage(
                    "BroadcastRaw",
                    player.uniqueId.toString(),
                    gson,
                    settings.joinPermission ?: "null",
                    settings.doubleTransfer.toString()
                )
            }
            return component to null
        }
        when (settings.target.range) {
            Target.Range.ALL -> {
                listeners.forEach {
                    getProxyPlayer(it)?.sendComponent(player, component)
                }
            }
            Target.Range.SINGLE_WORLD -> {
                onlinePlayers().filter { listeners.contains(it.uniqueId) && it.world == player.world.name }.forEach {
                    it.sendComponent(player, component)
                }
            }
            Target.Range.DISTANCE -> {
                onlinePlayers().filter { listeners.contains(it.uniqueId)
                        && it.world == player.world.name
                        && it.location.distance(player.location.toProxyLocation()) <= settings.target.distance }.forEach {
                    it.sendComponent(player, component)
                }
            }
            Target.Range.SELF -> {
                player.sendComponent(player, component)
            }
        }
        console().cast<CommandSender>().sendComponent(player, component)

        player.getSession().lastMessage = message
        ChatLogs.log(player, message)
        Metrics.increase(0)

        return component to null
    }

    open fun unregister() {
        bindings.command?.forEach { unregisterCommand(it) }
        listeners.clear()
    }

    companion object {

        val channels = mutableMapOf<String, Channel>()

        fun join(player: Player, channel: String, hint: Boolean = true) {
            channels[channel]?.let {
                join(player, it, hint)
            } ?: quit(player)
        }

        fun join(player: Player, channel: Channel, hint: Boolean = true) {
            if (channel.settings.joinPermission?.let { player.hasPermission(it) } == false) {
                player.sendLang("General-No-Permission")
                return
            }
            player.getSession().channel = channel.id
            channel.listeners.add(player.uniqueId)

            if (hint) {
                player.sendLang("Channel-Join", channel.id)
            }
        }

        fun quit(player: Player) {
            player.getSession().getChannel()?.let {
                if (!it.settings.autoJoin) {
                    it.listeners.remove(player.uniqueId)
                }
                player.sendLang("Channel-Quit", it.id)
            }
            player.getSession().channel = Settings.defaultChannel
        }
    }
}