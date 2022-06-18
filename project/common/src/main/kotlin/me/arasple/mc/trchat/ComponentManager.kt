package me.arasple.mc.trchat

import net.kyori.adventure.platform.AudienceProvider

/**
 * @author wlys
 * @since 2022/6/18 15:16
 */
interface ComponentManager {

    fun getAudienceProvider(): AudienceProvider

    fun init()

    fun release()

}