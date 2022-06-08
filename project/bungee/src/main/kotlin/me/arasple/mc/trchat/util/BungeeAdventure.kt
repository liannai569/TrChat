package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.TrChatBungee
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide

/**
 * @author wlys
 * @since 2022/6/8 13:05
 */
@Internal
@PlatformSide([Platform.BUNGEE])
object BungeeAdventure {

    lateinit var adventure: BungeeAudiences
        private set

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        adventure = BungeeAudiences.create(TrChatBungee.plugin)
    }

    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        adventure.close()
    }
}