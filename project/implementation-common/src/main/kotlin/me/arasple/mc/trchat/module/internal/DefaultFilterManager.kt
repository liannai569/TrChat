package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.FilterManager
import me.arasple.mc.trchat.module.display.filter.processer.Filter
import me.arasple.mc.trchat.module.display.filter.processer.FilteredObject
import me.arasple.mc.trchat.module.internal.service.Metrics
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyPlayer
import taboolib.common5.mirrorNow

@Awake
object DefaultFilterManager : FilterManager {

    init {
        PlatformFactory.registerAPI<FilterManager>(this)
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