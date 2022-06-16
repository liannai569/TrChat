package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.TrChat
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide

/**
 * @author wlys
 * @since 2022/6/8 12:56
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object BukkitAdventure {

    lateinit var adventure: BukkitAudiences
        private set

    fun init() {
        adventure = BukkitAudiences.create(TrChat.plugin)
    }

    fun release() {
        adventure.close()
    }
}