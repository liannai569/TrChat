package me.arasple.mc.trchat.module.internal.hook

import me.arasple.mc.trchat.module.internal.hook.impl.HookEcoEnchants
import me.arasple.mc.trchat.module.internal.hook.impl.HookInteractiveChat
import me.arasple.mc.trchat.module.internal.hook.impl.HookItemsAdder
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang

/**
 * @author Arasple
 * @date 2021/1/26 22:04
 */
@PlatformSide([Platform.BUKKIT])
object HookPlugin {

    val registry = arrayListOf(
        HookEcoEnchants(),
        HookItemsAdder(),
        HookInteractiveChat()
    )

    fun printInfo() {
        registry.filter { it.isHooked }.forEach {
            console().sendLang("Plugin-Dependency-Hooked", it.name)
        }
    }

    fun addHook(element: HookAbstract) {
        registry.add(element)
        console().sendLang("Plugin-Dependency-Hooked", element.name)
    }

    fun getEcoEnchants(): HookEcoEnchants {
        return registry[0] as HookEcoEnchants
    }

    fun getItemsAdder(): HookItemsAdder {
        return registry[1] as HookItemsAdder
    }

    fun getInteractiveChat(): HookInteractiveChat {
        return registry[2] as HookInteractiveChat
    }

}