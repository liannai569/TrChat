package me.arasple.mc.trchat

import me.arasple.mc.trchat.module.internal.service.Metrics
import net.md_5.bungee.api.ProxyServer
import taboolib.common.env.RuntimeEnv
import taboolib.common.platform.*
import taboolib.common.platform.command.command
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.common.platform.function.server
import taboolib.module.lang.sendLang
import taboolib.platform.BungeePlugin

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

        Metrics.init(5803)
    }

    override fun onEnable() {
        console().sendLang("Plugin-Enabled", pluginVersion)

        command("muteallservers", permission = "trchatb.muteallservers") {
            dynamic("state") {
                suggestion<ProxyCommandSender> { _, _ ->
                    listOf("on", "off")
                }
                execute<ProxyCommandSender> { _, _, argument ->
                    server<ProxyServer>().servers.forEach { (_, v) ->
                        BungeeProxyManager.sendTrChatMessage(v, "GlobalMute", argument)
                    }
                }
            }
        }
    }
}
