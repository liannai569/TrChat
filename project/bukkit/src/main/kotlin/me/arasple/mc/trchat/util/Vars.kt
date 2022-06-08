package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.api.config.Settings
import me.clip.placeholderapi.PlaceholderAPIPlugin
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.submit

/**
 * @author Arasple
 * @date 2019/11/29 21:29
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object Vars {

    @Schedule(async = true)
    fun downloadExpansions() {
        downloadExpansions(Settings.CONF.getStringList("Options.Depend-Expansions"))
    }

    /**
     * 自动下载 PlaceholderAPI 拓展变量并注册
     *
     * @param expansions 拓展
     */
    private fun downloadExpansions(expansions: List<String>) {
        kotlin.runCatching {
            if (expansions.isNotEmpty()) {
                if (PlaceholderAPIPlugin.getInstance().cloudExpansionManager.cloudExpansions.isEmpty()) {
                    PlaceholderAPIPlugin.getInstance().cloudExpansionManager.fetch(false)
                }
                val unInstalled = expansions.filter { d ->
                    PlaceholderAPIPlugin.getInstance().localExpansionManager.expansions.none { e -> e.name.equals(d, ignoreCase = true) }
                            && PlaceholderAPIPlugin.getInstance().cloudExpansionManager.findCloudExpansionByName(d).isPresent
                            && !PlaceholderAPIPlugin.getInstance().cloudExpansionManager.isDownloading(
                        PlaceholderAPIPlugin.getInstance().cloudExpansionManager.findCloudExpansionByName(d).get()
                    )
                }
                if (unInstalled.isNotEmpty()) {
                    unInstalled.forEach { ex ->
                        val cloudExpansion = PlaceholderAPIPlugin.getInstance().cloudExpansionManager.cloudExpansions[ex]!!
                        PlaceholderAPIPlugin.getInstance().cloudExpansionManager.downloadExpansion(cloudExpansion, cloudExpansion.version)
                    }
                    submit(delay = 20) {
                        PlaceholderAPIPlugin.getInstance().localExpansionManager.expansions.forEach {
                            PlaceholderAPIPlugin.getInstance().localExpansionManager.register(it)
                        }
                    }
                }
            }
        }.onFailure {
            it.print("PlaceholderAPI expansion(s) $expansions installed failed!Please install manually.")
        }
    }
}