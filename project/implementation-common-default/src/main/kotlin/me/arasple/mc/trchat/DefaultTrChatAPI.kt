package me.arasple.mc.trchat

import me.arasple.mc.trchat.module.display.filter.processer.Filter
import me.arasple.mc.trchat.module.display.filter.processer.FilteredObject
import me.arasple.mc.trchat.module.internal.service.Metrics
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common5.mirrorNow

/**
 * @author wlys
 * @since 2022/6/18 15:26
 */
@Awake
object DefaultTrChatAPI : TrChatAPI {

    init {
        TrChat.register(this)
    }

    override fun getComponentManager(): ComponentManager {
        return PlatformFactory.getAPI()
    }

    override fun getProxyManager(): ProxyManager {
        return PlatformFactory.getAPI()
    }

    override fun filterString(player: ProxyCommandSender, string: String, execute: Boolean): FilteredObject {
        return if (execute) {
            filter(string, !player.hasPermission("trchat.bypass.filter"))
        } else {
            FilteredObject(string, 0)
        }
    }


    override fun filter(string: String, execute: Boolean): FilteredObject {
        return mirrorNow("Handler:DoFilter") {
            Filter.doFilter(string, execute).also {
                Metrics.increase(1, it.sensitiveWords)
            }
        }
    }
}