package me.arasple.mc.trchat.module.conf.file

import me.arasple.mc.trchat.module.internal.service.Updater
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.submitAsync
import taboolib.common5.util.parseMillis
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.configuration.Configuration
import taboolib.module.kether.Kether

/**
 * @author ItsFlicker
 * @since 2021/12/11 23:59
 */
@PlatformSide([Platform.BUKKIT])
object Settings {

    @Config("settings.yml")
    lateinit var conf: Configuration
        private set

    @ConfigNode("Channel.Default", "settings.yml")
    var defaultChannel = "Normal"

    @ConfigNode("Chat.Cooldown", "settings.yml")
    val chatCooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("Chat.Anti-Repeat", "settings.yml")
    var chatSimilarity = 0.85

    @ConfigNode("Chat.Length-Limit", "settings.yml")
    var chatLengthLimit = 100

    @ConfigNode("Options.Component-Max-Length", "settings.yml")
    var componentMaxLength = 32766

    @Awake(LifeCycle.ENABLE)
    fun init() {
        conf.onReload {
            Kether.isAllowToleranceParser = conf.getBoolean("Options.Kether-Allow-Tolerance-Parser", true)
        }
        if (conf.getBoolean("Options.Check-Update", true)) {
            submitAsync(delay = 20, period = 15 * 60 * 20) {
                Updater.grabInfo()
            }
        }
    }

}