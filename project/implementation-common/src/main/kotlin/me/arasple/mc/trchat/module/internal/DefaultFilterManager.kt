package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.FilterManager
import me.arasple.mc.trchat.module.internal.filter.processer.Filter
import me.arasple.mc.trchat.module.internal.filter.processer.FilteredObject
import me.arasple.mc.trchat.module.internal.service.Metrics
import me.arasple.mc.trchat.util.parseJson
import me.arasple.mc.trchat.util.reportOnce
import taboolib.common.env.DependencyDownloader
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.submitAsync
import taboolib.common.util.decodeUnicode
import taboolib.common5.mirrorNow
import taboolib.module.lang.sendLang
import java.io.BufferedInputStream
import java.net.URL
import java.nio.charset.StandardCharsets

@Awake
object DefaultFilterManager : FilterManager {

    init {
        PlatformFactory.registerAPI<FilterManager>(this)
    }

    var isCloudEnabled = true
    val cloud_url = mutableSetOf<String>()
    val ignored_cloud_words = mutableSetOf<String>()
    val custom_replacements = mutableMapOf<String, String>()
    val white_words = mutableListOf<String>()

    private val cloud_words = mutableSetOf<String>()
    private val cloud_last_update = mutableMapOf<String, String>()

    private var initialized = false

    override fun loadFilter(
        localWords: List<String>,
        punctuations: List<String>,
        replacement: Char,
        isCloudEnabled: Boolean,
        cloudUrls: List<String>,
        ignoredCloudWords: List<String>,
        updateCloud: Boolean,
        notify: ProxyCommandSender?
    ) {
        // 初始化本地配置
        Filter.setSensitiveWord(localWords)
        Filter.setPunctuations(punctuations)
        Filter.setReplacement(replacement)

        this.isCloudEnabled = isCloudEnabled
        cloud_url += cloudUrls
        this.ignored_cloud_words += ignoredCloudWords

        notify?.sendLang("Plugin-Loaded-Filter-Local", localWords.size)

        if (!initialized) {
            submitAsync(period = (60 * 60 * 20).toLong()) {
                loadCloudThesaurus(notify)
            }
            initialized = true
        }

        // 更新云端词库
        if (updateCloud) {
            submitAsync {
                loadCloudThesaurus(notify)
            }
        }
    }

    override fun loadCloudThesaurus(notify: ProxyCommandSender?) {
        if (!isCloudEnabled || cloud_url.isEmpty()) {
            return
        }
        val collected = mutableSetOf<String>()
        cloud_url.forEach {
            collected += catchCloudThesaurus(it, notify)
        }
        cloud_words += collected
        if (cloud_words.isEmpty()) {
            notify?.sendLang("Plugin-Failed-Load-Filter-Cloud")
        } else {
            Filter.addSensitiveWord(cloud_words.sortedByDescending { it.length })
        }
    }

    private fun catchCloudThesaurus(url: String, notify: ProxyCommandSender?): List<String> {
        val collected = mutableListOf<String>()

        return kotlin.runCatching {
            URL(url).openConnection().also { it.connectTimeout = 60 * 1000 }.getInputStream().use { inputStream ->
                BufferedInputStream(inputStream).use { bufferedInputStream ->
                    val database = DependencyDownloader.readFully(bufferedInputStream, StandardCharsets.UTF_8).parseJson().asJsonObject
                    require(database.has("lastUpdateDate") && database.has("words")) {
                        "Wrong database json object"
                    }

                    val lastUpdateDate = database["lastUpdateDate"].asString
                    cloud_last_update[url] = when (cloud_last_update[url]) {
                        null -> lastUpdateDate
                        lastUpdateDate -> return emptyList()
                        else -> lastUpdateDate
                    }
                    database["words"].asJsonArray.forEach {
                        val word = it.asString.decodeUnicode()
                        if (ignored_cloud_words.none { w -> w.equals(word, ignoreCase = true) }) {
                            collected.add(word)
                        }
                    }
                }
            }
            notify?.sendLang("Plugin-Loaded-Filter-Cloud", collected.size, url, cloud_last_update[url]!!)
            collected
        }.getOrElse {
            it.reportOnce("Error occurred while catching cloud thesaurus of $url.", printStackTrace = false)
            emptyList()
        }
    }

    override fun filter(string: String, execute: Boolean, player: ProxyPlayer?): FilteredObject {
        return if (execute && player?.hasPermission("trchat.bypass.filter") != true) {
            mirrorNow("Handler:DoFilter") {
                Filter.doFilter(string).also {
                    Metrics.increase(1, it.sensitiveWords)
                }
            }
        } else {
            FilteredObject(string, 0)
        }
    }

}