package me.arasple.mc.trchat.module.conf.file

import me.arasple.mc.trchat.TrChat
import org.bukkit.command.CommandSender
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.console
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

/**
 * @author ItsFlicker
 * @since 2022/2/4 13:04
 */
@PlatformSide([Platform.BUKKIT])
object Filters {

    @Config("filter.yml")
    lateinit var CONF: Configuration
        private set

    @Awake(LifeCycle.ENABLE)
    fun init() {
        CONF.onReload { reload() }
        reload()
    }

    fun reload(notify: CommandSender? = null) {
        TrChat.api().getFilterManager().loadFilter(
            CONF.getStringList("Local"),
            CONF.getStringList("Ignored-Punctuations"),
            CONF.getString("Replacement", "*")!![0],
            CONF.getBoolean("Cloud-Thesaurus.Enabled"),
            CONF.getStringList("Cloud-Thesaurus.Urls"),
            CONF.getStringList("Cloud-Thesaurus.Ignored"),
            notify = notify?.let { adaptCommandSender(it) } ?: console()
        )
    }
}