package me.arasple.mc.trchat.module.internal.filter

import me.arasple.mc.trchat.module.conf.file.Filters
import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.module.internal.filter.processer.Filter
import me.arasple.mc.trchat.util.parseJson
import me.arasple.mc.trchat.util.print
import taboolib.common.env.DependencyDownloader.readFully
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendLang
import java.io.BufferedInputStream
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * @author Arasple
 * @date 2019/10/12 20:52
 */
@PlatformSide([Platform.BUKKIT])
object ChatFilter {

    private val custom_replacements = mutableMapOf<String, String>()
    private val cloud_words = mutableListOf<String>()
    private val white_words = mutableListOf<String>()
    private val cloud_last_update = mutableMapOf<String, String>()
    private var cloud_url = listOf<String>()

    @Schedule(delay = (20 * 120).toLong(), period = (30 * 60 * 20).toLong(), async = true)
    fun asyncRefreshCloud() {
        loadCloudFilter()
    }

    /**
     * 加载聊天过滤器
     *
     * @param updateCloud 是否更新云端词库
     * @param notify      接受通知反馈
     */
    fun loadFilter(updateCloud: Boolean, notify: ProxyCommandSender = console()) {
        // 初始化本地配置
        Filter.setSensitiveWord(Filters.CONF.getStringList("Local"))
        Filter.setPunctuations(Filters.CONF.getStringList("Ignored-Punctuations"))
        Filter.setReplacement(Filters.CONF.getString("Replacement")!![0])

        notify.sendLang("Plugin-Loaded-Filter-Local", Filters.CONF.getStringList("Local").size)

        // 更新云端词库
        if (updateCloud && Filters.cloud_enabled) {
            cloud_url = Filters.CONF.getStringList("Cloud-Thesaurus.Urls")
            submit(async = true) {
                loadCloudFilter(notify)
            }
        }
    }

    /**
     * 加载云端聊天敏感词库
     *
     * @param notify 接受通知反馈
     */
    private fun loadCloudFilter(notify: ProxyCommandSender = console()) {
        if (cloud_url.isEmpty()) {
            return
        }
        val collected = mutableListOf<String>()
        cloud_url.forEach {
            collected += catchCloudThesaurus(it, notify)
        }
        cloud_words += collected
        if (cloud_words.isEmpty()) {
            notify.sendLang("Plugin-Failed-Load-Filter-Cloud")
        } else {
            Filter.addSensitiveWord(cloud_words)
        }
    }

    /**
     * 抓取云端聊天敏感词库
     *
     * @param url    尝试 URL 序号
     * @param notify 接受通知反馈
     */
    private fun catchCloudThesaurus(url: String, notify: ProxyCommandSender): List<String> {
        val whitelist = Filters.CONF.getStringList("Cloud-Thesaurus.Ignored")
        val collected = mutableListOf<String>()

        return kotlin.runCatching {
            URL(url).openConnection().also { it.connectTimeout = 60 * 1000 }.getInputStream().use { inputStream ->
                BufferedInputStream(inputStream).use { bufferedInputStream ->
                    val database = readFully(bufferedInputStream, StandardCharsets.UTF_8).parseJson().asJsonObject
                    if (!database.has("lastUpdateDate") || !database.has("words")) {
                        error("Wrong database json object")
                    }

                    val lastUpdateDate = database["lastUpdateDate"].asString
                    cloud_last_update[url] = when (cloud_last_update[url]) {
                        null -> lastUpdateDate
                        lastUpdateDate -> return emptyList()
                        else -> lastUpdateDate
                    }
                    database["words"].asJsonArray.forEach {
                        val word = it.asString
                        if (whitelist.none { w -> w.equals(word, ignoreCase = true) }) {
                            collected.add(word)
                        }
                    }
                }
            }
            notify.sendLang("Plugin-Loaded-Filter-Cloud", collected.size, url, cloud_last_update[url]!!)
            collected
        }.getOrElse {
            if (!TrChatBukkit.reportedErrors.contains("catchCloudThesaurus")) {
                it.print("Error occurred while catching cloud thesaurus.", printStackTrace = false)
                TrChatBukkit.reportedErrors.add("catchCloudThesaurus")
            }
            emptyList()
        }
    }
}