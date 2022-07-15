package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.ProxyManager
import me.arasple.mc.trchat.util.buildMessage
import me.arasple.mc.trchat.util.print
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.server
import taboolib.common.platform.function.submit
import java.io.IOException

/**
 * @author wlys
 * @since 2022/6/18 19:21
 */
@PlatformSide([Platform.BUNGEE])
object BungeeProxyManager : ProxyManager {

    init {
        PlatformFactory.registerAPI<ProxyManager>(this)
        server<ProxyServer>().registerChannel(TrChatBungee.TRCHAT_CHANNEL)
    }

    override fun sendCommonMessage(recipient: Any, vararg args: String, async: Boolean): Boolean {
        error("Not supported.")
    }

    override fun sendTrChatMessage(recipient: Any, vararg args: String, async: Boolean): Boolean {
        if (recipient !is ServerInfo) return false
        var success = true
        submit(async = async) {
            try {
                for (bytes in buildMessage(*args)) {
                    recipient.sendData(TrChatBungee.TRCHAT_CHANNEL, bytes)
                }
            } catch (e: IOException) {
                e.print("Failed to send proxy trchat message!")
                success = false
            }
        }

        return success
    }

}