package me.arasple.mc.trchat.module.internal.data

import me.arasple.mc.trchat.api.event.CustomDatabaseEvent
import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.internal.database.DatabaseSQL
import me.arasple.mc.trchat.module.internal.database.DatabaseSQLite
import me.arasple.mc.trchat.util.Internal
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Schedule
import taboolib.common.util.unsafeLazy
import taboolib.platform.util.onlinePlayers

/**
 * @author wlys
 * @since 2021/9/11 13:29
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object Databases {

    val database by unsafeLazy {
        when (val type = Settings.CONF.getString("Database.Method")?.uppercase()) {
            "LOCAL", "SQLITE", null -> DatabaseSQLite()
            "SQL" -> DatabaseSQL()
            else -> {
                val event = CustomDatabaseEvent(type)
                event.call()
                event.database ?: error("Unsupported database type: $type")
            }
        }
    }

    @Schedule(delay = 100, period = (20 * 60 * 5).toLong(), async = true)
    @Awake(LifeCycle.DISABLE)
    fun save() {
        onlinePlayers.forEach { database.push(it) }
    }

}