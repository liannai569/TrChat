package me.arasple.mc.trchat.module.internal.service

import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.parseJson
import taboolib.common.LifeCycle
import taboolib.common.env.DependencyDownloader.readFully
import taboolib.common.platform.Schedule
import taboolib.common.platform.SkipTo
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.common.util.Version
import taboolib.module.lang.sendLang
import java.io.BufferedInputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author Arasple
 * @date 2019/11/29 21:04
 */
@Internal
@SkipTo(LifeCycle.LOAD)
object Updater {

    private const val api_url = "https://api.github.com/repos/TrPlugins/TrChat/releases/latest"
    private var notify = false
    val notified = mutableListOf<UUID>()
    val current_version = Version(pluginVersion)
    var latest_Version = Version("0.0")
    var information = ""

    @Schedule(delay = 20, period = (15 * 60 * 20).toLong(), async = true)
    fun grabInfo() {
        if (latest_Version.version[0] > 0) {
            return
        }
        kotlin.runCatching {
            URL(api_url).openStream().use { inputStream ->
                BufferedInputStream(inputStream).use { bufferedInputStream ->
                    val read = readFully(bufferedInputStream, StandardCharsets.UTF_8)
                    val json = read.parseJson().asJsonObject
                    val latestVersion = json["tag_name"].asString.substring(1)
                    latest_Version = Version(latestVersion)
                    information = json["body"].asString
                    notifyVersion()
                }
            }
        }
    }

    private fun notifyVersion() {
        if (latest_Version > current_version) {
            console().sendLang("Plugin-Updater-Header", current_version, latest_Version)
            console().sendMessage(information)
            console().sendLang("Plugin-Updater-Footer")
        } else {
            if (!notify) {
                notify = true
                if (current_version > latest_Version) {
                    console().sendLang("Plugin-Updater-Dev")
                } else {
                    console().sendLang("Plugin-Updater-Latest")
                }
            }
        }
    }
}