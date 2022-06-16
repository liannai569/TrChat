package me.arasple.mc.trchat.module.internal.hook

import me.arasple.mc.trchat.module.internal.hook.impl.HookEcoEnchants
import me.arasple.mc.trchat.module.internal.hook.impl.HookInteractiveChat
import me.arasple.mc.trchat.module.internal.hook.impl.HookItemsAdder
import me.arasple.mc.trchat.util.Internal
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang

/**
 * @author Arasple
 * @date 2021/1/26 22:04
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object HookPlugin {

    fun printInfo() {
        registry.filter { it.isHooked }.forEach {
            console().sendLang("Plugin-Dependency-Hooked", it.name)
        }
    }

    private val registry: Array<HookAbstract> = arrayOf(
        HookEcoEnchants(),
        HookItemsAdder(),
        HookInteractiveChat()
    )

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