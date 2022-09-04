package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.BukkitEnv
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.conf.Loader
import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.internal.data.Databases
import me.arasple.mc.trchat.module.internal.data.PlayerData
import me.arasple.mc.trchat.module.internal.filter.ChatFilter
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyManager
import me.arasple.mc.trchat.module.internal.service.Metrics
import me.arasple.mc.trchat.util.color.parseGradients
import me.arasple.mc.trchat.util.color.parseRainbow
import org.bukkit.Bukkit
import taboolib.common.env.RuntimeEnv
import taboolib.common.platform.*
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.module.kether.Kether
import taboolib.module.lang.Language
import taboolib.module.lang.TextTransfer
import taboolib.module.lang.sendLang
import taboolib.module.nms.MinecraftVersion.majorLegacy

/**
 * @author Arasple
 */
@PlatformSide([Platform.BUKKIT])
object TrChatBukkit : Plugin() {

    var paperEnv = false
        private set

    var isGlobalMuting = false

    val reportedErrors = mutableListOf<String>()

    @Awake
    fun loadDependency() {
        try {
            // Paper 1.16.5+
            Class.forName("com.destroystokyo.paper.PaperConfig")
            if (majorLegacy >= 11604) {
                paperEnv = true
            }
        } catch (_: ClassNotFoundException) {
        }
        if (!paperEnv) {
            RuntimeEnv.ENV.loadDependency(BukkitEnv::class.java, true)
        }
    }

    override fun onLoad() {
        console().sendLang("Plugin-Loading", Bukkit.getBukkitVersion())

        Metrics.init(5802)
    }

    override fun onEnable() {
        Databases.init()
        if (!paperEnv) {
            BukkitComponentManager.init()
        }
        BukkitProxyManager.platform

        Kether.isAllowToleranceParser = Settings.CONF.getBoolean("Options.Kether-Allow-Tolerance-Parser", true)
        Language.textTransfer += object : TextTransfer {
            override fun translate(sender: ProxyCommandSender, source: String): String {
                return source.parseRainbow().parseGradients()
            }
        }

        HookPlugin.printInfo()
        TrChat.api().getChannelManager().loadChannels(console())
        Loader.loadFunctions(console())
        ChatFilter.loadFilter(true, console())

        console().sendLang("Plugin-Enabled", pluginVersion)
    }

    override fun onDisable() {
        if (!paperEnv) {
            BukkitComponentManager.release()
        }
        BukkitProxyManager.processor?.close()

        ChatSession.SESSIONS.clear()
        PlayerData.DATA.clear()
        Channel.channels.clear()
        Function.functions.clear()
    }
}