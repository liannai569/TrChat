package me.arasple.mc.trchat.util

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common5.FileWatcher
import java.io.File

/**
 * @author wlys
 * @since 2022/4/4 13:50
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object FileListener {

    val watcher by lazy { FileWatcher() }

    fun isListening(file: File): Boolean {
        return watcher.hasListener(file)
    }

    fun listen(file: File, runFirst: Boolean = false, runnable: Runnable) {
        watcher.addSimpleListener(file, runnable, runFirst)
    }

    @Awake(LifeCycle.DISABLE)
    fun unregister() {
        watcher.unregisterAll()
    }
}