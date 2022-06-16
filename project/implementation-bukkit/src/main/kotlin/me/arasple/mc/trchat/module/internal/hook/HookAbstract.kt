package me.arasple.mc.trchat.module.internal.hook

import me.arasple.mc.trchat.util.Internal
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

/**
 * @author Arasple
 * @date 2021/1/26 22:02
 */
@Internal
abstract class HookAbstract {

    open val name by lazy { getPluginName() }

    val plugin: Plugin? by lazy {
        Bukkit.getPluginManager().getPlugin(name)
    }

    val isHooked by lazy {
        plugin != null && plugin!!.isEnabled
    }

    open fun getPluginName(): String {
        return javaClass.simpleName.substring(4)
    }

}