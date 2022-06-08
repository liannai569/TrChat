package me.arasple.mc.trchat

import me.arasple.mc.trchat.util.buildMessage
import net.md_5.bungee.api.ProxyServer
import taboolib.common.env.RuntimeEnv
import taboolib.common.platform.*
import taboolib.common.platform.command.command
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.common.platform.function.server
import taboolib.module.lang.sendLang
import taboolib.module.metrics.Metrics
import taboolib.platform.BungeePlugin
import java.io.IOException

/**
 * @author Arasple
 * @date 2019/8/4 22:42
 */
@PlatformSide([Platform.BUNGEE])
object TrChatBungee : Plugin() {

    val plugin by lazy { BungeePlugin.getInstance() }

    const val TRCHAT_CHANNEL = "trchat:main"

    @Awake
    fun loadDependency() {
        RuntimeEnv.ENV.loadDependency(BungeeEnv::class.java, true)
    }

    override fun onLoad() {
        ProxyServer.getInstance().registerChannel(TRCHAT_CHANNEL)

        console().sendLang("Plugin-Loading", server<ProxyServer>().version)
        console().sendLang("Plugin-Proxy-Supported", "Bungee")

        Metrics(5803, pluginVersion, Platform.BUNGEE)
    }

    override fun onEnable() {
        console().sendLang("Plugin-Enabled", pluginVersion)

        command("muteallservers", permission = "trchatb.muteallservers") {
            dynamic("state") {
                suggestion<ProxyCommandSender> { _, _ ->
                    listOf("on", "off")
                }
                execute<ProxyCommandSender> { _, _, argument ->
                    try {
                        server<ProxyServer>().servers.forEach { (_, v) ->
                            for (bytes in buildMessage("GlobalMute", argument)) {
                                v.sendData(TRCHAT_CHANNEL, bytes)
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
