package me.arasple.mc.trchat.api

import taboolib.common.platform.function.onlinePlayers
import java.util.concurrent.ExecutorService

interface ProxyMessageManager {

    val allPlayerNames get() = onlinePlayers().map { it.name }

    val executor: ExecutorService

    val allDisplayNames: MutableMap<String, String>

    fun updateAllNames()

}