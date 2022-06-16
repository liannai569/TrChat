package me.arasple.mc.trchat.api.config

import me.arasple.mc.trchat.module.display.channel.Channel
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.severe
import taboolib.common5.util.parseMillis
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.configuration.Configuration

/**
 * @author wlys
 * @since 2021/12/11 23:59
 */
@PlatformSide([Platform.BUKKIT])
object Settings {

    @Config("settings.yml", autoReload = true)
    lateinit var CONF: Configuration
        private set

    @Awake(LifeCycle.LOAD)
    fun init() {
        CONF.onReload {
            onReload()
        }
    }

    fun onReload() {
        val id = CONF.getString("Channel.Default", "Normal")!!
        Channel.defaultChannel = Channel.channels.firstOrNull { it.id.equals(id, ignoreCase = true) }.also {
            if (it == null) severe("Default channel $id not found.")
        }
    }

    @ConfigNode("Chat.Cooldown", "settings.yml")
    val chatCooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("Chat.Anti-Repeat", "settings.yml")
    var chatSimilarity = 0.85

    @ConfigNode("Chat.Length-Limit", "settings.yml")
    var chatLengthLimit = 100
}