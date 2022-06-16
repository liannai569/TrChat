package me.arasple.mc.trchat.module.internal.data

import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.util.Internal
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.getDataFolder
import taboolib.common.util.replaceWithOrder
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

/**
 * @author Arasple
 * @date 2019/11/30 16:08
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object ChatLogs {

    private val waveList = mutableListOf<String>()
    private val dateFormat0 = SimpleDateFormat("yyyy-MM-dd")
    private val dateFormat1 = SimpleDateFormat("HH:mm:ss")

    @Schedule(delay = (20 * 15).toLong(), period = (20 * 60 * 5).toLong(), async = true)
    @Awake(LifeCycle.DISABLE)
    fun writeToFile() {
        val logFile = newFile(File(getDataFolder(), "logs"), "${dateFormat0.format(System.currentTimeMillis())}.txt", create = true)
        logFile.writer().use {
            waveList.forEach { line ->
                it.write(line)
                it.appendLine()
            }
        }
        waveList.clear()
    }

    @Schedule(delay = (20 * 15).toLong(), period = (20 * 60 * 5).toLong(), async = true)
    fun autoDelete() {
        val days = Settings.CONF.getLong("General.Log-Delete-Time", 0L)
        if (days > 0) {
            val millis = TimeUnit.DAYS.toMillis(days)
            kotlin.runCatching {
                File(getDataFolder(), "logs").walk().forEach {
                    if (it.lastModified() < (System.currentTimeMillis() - millis)) {
                        it.delete()
                    }
                }
            }
        }
    }

    fun log(player: Player, originalMessage: String) {
        waveList.add(
            Settings.CONF.getString("General.Log-Normal", "[{0}] {1}: {2}")!!.replaceWithOrder(
                dateFormat1.format(System.currentTimeMillis()),
                player.name,
                originalMessage
            )
        )
    }

    fun logPrivate(from: String, to: String, originalMessage: String) {
        waveList.add(
            Settings.CONF.getString("General.Log-Private", "[{0}] {1} -> {2}: {3}")!!.replaceWithOrder(
                dateFormat1.format(System.currentTimeMillis()),
                from,
                to,
                originalMessage
            )
        )
    }
}