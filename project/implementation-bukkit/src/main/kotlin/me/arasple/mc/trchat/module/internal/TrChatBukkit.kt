package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.BukkitEnv
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.module.conf.Loader
import me.arasple.mc.trchat.module.display.filter.ChatFilter
import me.arasple.mc.trchat.module.internal.data.Databases
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyManager
import me.arasple.mc.trchat.module.internal.service.Metrics
import org.bukkit.Bukkit
import taboolib.common.env.RuntimeEnv
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.module.kether.Kether
import taboolib.module.lang.sendLang
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.nmsClass
import taboolib.module.nms.obcClass
import taboolib.platform.BukkitPlugin

/**
 * @author Arasple
 */
@PlatformSide([Platform.BUKKIT])
object TrChatBukkit : Plugin() {

    val plugin by lazy { BukkitPlugin.getInstance() }

    var paperEnv = false
        private set

    var isGlobalMuting = false

    val reportedErrors = mutableListOf<String>()

    val classCraftItemStack by lazy {
        obcClass("inventory.CraftItemStack")
    }

    val classChatSerializer by lazy {
        nmsClass("IChatBaseComponent\$ChatSerializer")
    }

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
        if (!paperEnv) {
            BukkitComponentManager.init()
        }
        Kether.isAllowToleranceParser = Settings.CONF.getBoolean("Options.Kether-Allow-Tolerance-Parser", true)

        Databases.init()

        TrChat.api().getChannelManager().loadChannels(console())
        Loader.loadFunctions(console())
        ChatFilter.loadFilter(true, console())

        BukkitProxyManager.platform
        HookPlugin.printInfo()
        console().sendLang("Plugin-Enabled", pluginVersion)
    }

    override fun onDisable() {
        if (!paperEnv) {
            BukkitComponentManager.release()
        }
    }
}