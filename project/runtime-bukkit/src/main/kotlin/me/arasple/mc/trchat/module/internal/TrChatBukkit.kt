package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.conf.Loader
import me.arasple.mc.trchat.module.conf.file.Filters
import me.arasple.mc.trchat.module.conf.file.Functions
import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.internal.data.Databases
import me.arasple.mc.trchat.module.internal.data.PlayerData
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyManager
import me.arasple.mc.trchat.module.internal.redis.RedisManager
import org.bukkit.Bukkit
import taboolib.common.platform.*
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.module.kether.Kether
import taboolib.module.lang.sendLang
import taboolib.module.nms.MinecraftVersion.majorLegacy

/**
 * @author Arasple
 */
@PlatformSide([Platform.BUKKIT])
object TrChatBukkit : Plugin() {

    var isPaperEnv = false
        private set

    var isGlobalMuting = false

    @Awake
    internal fun detectPaperEnv() {
        try {
            // Paper 1.16.5+
            Class.forName("com.destroystokyo.paper.PaperConfig")
            if (majorLegacy >= 11604) {
                isPaperEnv = true
            }
        } catch (_: ClassNotFoundException) {
        }
    }

    override fun onLoad() {
        console().sendLang("Plugin-Loading", Bukkit.getBukkitVersion())
    }

    override fun onEnable() {
        Databases.database

        if (RedisManager.enabled) {
            RedisManager()
        } else {
            BukkitProxyManager.processor
        }

        HookPlugin.printInfo()

        reload(console())

        console().sendLang("Plugin-Enabled", pluginVersion)
    }

    override fun onDisable() {
        BukkitProxyManager.close()

        ChatSession.sessions.clear()
        PlayerData.data.clear()
        Channel.channels.clear()
        Function.functions.clear()
    }

    fun reload(notify: ProxyCommandSender) {
        Settings.CONF.reload()
        Functions.CONF.reload()
        Filters.CONF.reload()
        Kether.isAllowToleranceParser = Settings.CONF.getBoolean("Options.Kether-Allow-Tolerance-Parser", true)
        TrChat.api().getChannelManager().loadChannels(notify)
        Loader.loadFunctions(notify)
    }

}