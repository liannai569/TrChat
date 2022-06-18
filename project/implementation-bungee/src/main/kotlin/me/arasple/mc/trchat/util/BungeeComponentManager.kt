package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.ComponentManager
import me.arasple.mc.trchat.TrChatBungee
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide

/**
 * @author wlys
 * @since 2022/6/8 13:05
 */
@Internal
@PlatformSide([Platform.BUNGEE])
object BungeeComponentManager : ComponentManager {

    init {
        PlatformFactory.registerAPI<ComponentManager>(this)
    }

    private var adventure: BungeeAudiences? = null

    override fun getAudienceProvider(): BungeeAudiences {
        return adventure!!
    }

    override fun init() {
        adventure = BungeeAudiences.create(TrChatBungee.plugin)
    }

    override fun release() {
        adventure?.close()
    }
}