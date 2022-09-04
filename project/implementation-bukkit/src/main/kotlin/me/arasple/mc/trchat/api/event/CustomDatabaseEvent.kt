package me.arasple.mc.trchat.api.event

import me.arasple.mc.trchat.module.internal.database.Database
import taboolib.platform.type.BukkitProxyEvent

class CustomDatabaseEvent(val name: String, var database: Database? = null) : BukkitProxyEvent() {

    override val allowCancelled: Boolean
        get() = false
}