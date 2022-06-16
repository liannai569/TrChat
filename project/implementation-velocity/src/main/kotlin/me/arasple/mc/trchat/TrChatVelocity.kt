package me.arasple.mc.trchat

import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import me.arasple.mc.trchat.util.buildMessage
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.command
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.pluginVersion
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendLang
import taboolib.module.metrics.Metrics
import taboolib.platform.VelocityPlugin
import java.io.IOException

/**
 * TrChatVelocity
 * me.arasple.mc.trchat
 *
 * @author wlys
 * @since 2021/8/21 13:42
 */
@PlatformSide([Platform.VELOCITY])
object TrChatVelocity : Plugin() {

    val plugin by lazy { VelocityPlugin.getInstance() }

    lateinit var incoming: MinecraftChannelIdentifier
    lateinit var outgoing: MinecraftChannelIdentifier

    override fun onLoad() {
        console().sendLang("Plugin-Loading", plugin.server.version.version)

        incoming = MinecraftChannelIdentifier.create("trchat", "proxy").also {
            plugin.server.channelRegistrar.register(it)
        }
        outgoing = MinecraftChannelIdentifier.create("trchat", "server").also {
            plugin.server.channelRegistrar.register(it)
        }

        console().sendLang("Plugin-Proxy-Supported", "Velocity")
    }

    override fun onEnable() {
        console().sendLang("Plugin-Enabled", pluginVersion)
        Metrics(12541, pluginVersion, Platform.VELOCITY)

        command("muteallservers", permission = "trchatv.muteallservers") {
            dynamic("state") {
                suggestion<ProxyCommandSender> { _, _ ->
                    listOf("on", "off")
                }
                execute<ProxyCommandSender> { _, _, argument ->
                    try {
                        plugin.server.allServers.forEach { server ->
                            for (bytes in buildMessage("GlobalMute", argument)) {
                                server.sendPluginMessage(outgoing, bytes)
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        submit(period = 60, async = true) {
            try {
                plugin.server.allServers.forEach { server ->
                    for (bytes in buildMessage("PlayerList", onlinePlayers().joinToString(", ") { it.name })) {
                        server.sendPluginMessage(outgoing, bytes)
                    }
                }
            } catch (_: IOException) {
            }
        }
    }
}