package me.arasple.mc.trchat.module.conf

import me.arasple.mc.trchat.ChannelManager
import me.arasple.mc.trchat.module.internal.BungeeProxyManager
import me.arasple.mc.trchat.util.print
import net.md_5.bungee.api.ProxyServer
import taboolib.common.io.newFile
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.server
import taboolib.common.util.unsafeLazy
import taboolib.common5.FileWatcher
import taboolib.module.lang.sendLang
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * @author ItsFlicker
 * @since 2022/6/19 20:26
 */
@PlatformSide([Platform.BUNGEE])
object BungeeChannelManager : ChannelManager {

    init {
        PlatformFactory.registerAPI<ChannelManager>(this)
    }

    private val folder by unsafeLazy {
        val folder = File(getDataFolder(), "channels")

        if (!folder.exists()) {
            releaseResourceFile("channels/Bungee.yml", replace = true)
            newFile(File(getDataFolder(), "data"), folder = true)
        }

        folder
    }

    val channels = mutableMapOf<String, String>()
    val loadedServers = mutableMapOf<String, ArrayList<Int>>()

    override fun loadChannels(sender: ProxyCommandSender) {
        measureTimeMillis {
            channels.clear()

            filterChannelFiles(folder).forEach {
                val id = it.nameWithoutExtension
                if (FileWatcher.INSTANCE.hasListener(it)) {
                    try {
                        val channel = it.readText()
                        channels[id] = channel
                        loadedServers[id]?.clear()
                        sendProxyChannel(id, channel)
                    } catch (t: Throwable) {
                        t.print("Channel file ${it.name} loaded failed!")
                    }
                } else {
                    FileWatcher.INSTANCE.addSimpleListener(it, {
                        try {
                            val channel = it.readText()
                            channels[id] = channel
                            loadedServers[id]?.clear()
                            sendProxyChannel(id, channel)
                        } catch (t: Throwable) {
                            t.print("Channel file ${it.name} loaded failed!")
                        }
                    }, true)
                }
            }
        }.let {
            sender.sendLang("Plugin-Loaded-Channels", channels.size, it)
        }
    }

    override fun getChannel(id: String): String? {
        return channels[id]
    }

    @Suppress("Deprecation")
    fun sendProxyChannel(id: String, channel: String) {
        server<ProxyServer>().servers.filterNot {
            loadedServers.computeIfAbsent(id) { ArrayList() }.contains(it.value.address.port)
        }.forEach { (_, server) ->
            BungeeProxyManager.sendTrChatMessage(server, "SendProxyChannel", id, channel)
        }
    }

    @Suppress("Deprecation")
    fun sendAllProxyChannels(port: Int) {
        val server = server<ProxyServer>().servers.values.firstOrNull { it.address.port == port } ?: return
        channels.forEach {
            BungeeProxyManager.sendTrChatMessage(server, "SendProxyChannel", it.key, it.value)
        }
    }

    private fun filterChannelFiles(file: File): List<File> {
        return mutableListOf<File>().apply {
            if (file.isDirectory) {
                file.listFiles()?.forEach {
                    addAll(filterChannelFiles(it))
                }
            } else if (file.extension.equals("yml", true)) {
                add(file)
            }
        }
    }

}