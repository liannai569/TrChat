package me.arasple.mc.trchat.module.internal.data

import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.module.internal.database.Database
import me.arasple.mc.trchat.module.internal.database.DatabaseMongodb
import me.arasple.mc.trchat.module.internal.database.DatabaseSQL
import me.arasple.mc.trchat.module.internal.database.DatabaseSQLite
import me.arasple.mc.trchat.util.Internal
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.onlinePlayers

/**
 * @author wlys
 * @since 2021/9/11 13:29
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object Database {

    lateinit var database: Database

    fun init() {
        database = when (val type = Settings.CONF.getString("Database.Method")!!.uppercase()) {
            "SQLITE" -> DatabaseSQLite()
            "SQL" -> DatabaseSQL()
            "MONGODB" -> DatabaseMongodb()
            else -> error("Unsupported database type: $type")
        }
    }

    @Schedule(delay = 100, period = 20 * 60 * 5, async = true)
    @Awake(LifeCycle.DISABLE)
    fun save() {
        onlinePlayers().forEach { database.push(it.cast()) }
    }
}