package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.ComponentManager
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.gson
import me.arasple.mc.trchat.util.sendChatComponent
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import java.util.*

/**
 * @author wlys
 * @since 2022/6/8 12:56
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object BukkitComponentManager : ComponentManager {

    init {
        PlatformFactory.registerAPI<ComponentManager>(this)
    }

    private var adventure: BukkitAudiences? = null

    override fun getAudienceProvider(): BukkitAudiences {
        return adventure!!
    }

    override fun init() {
        adventure = BukkitAudiences.create(TrChatBukkit.plugin)
    }

    override fun release() {
        adventure?.close()
    }

    override fun filterComponent(component: Component?, maxLength: Int): Component? {
        component ?: return null
        val newComponent = if (component is TextComponent && component.content().isNotEmpty()) {
            component.content(TrChat.api().filter(component.content()).filtered)
        } else {
            component
        }
        return validateComponent(if (newComponent.children().isNotEmpty()) {
            Component.text { builder ->
                newComponent.children().forEach { builder.append(filterComponent(it, -1)!!) }
                builder.style(newComponent.style())
                if (newComponent is TextComponent) {
                    builder.content(newComponent.content())
                }
            }
        } else {
            newComponent
        }, maxLength)
    }

    override fun sendChatComponent(receiver: ProxyCommandSender, component: Component, sender: UUID) {
        receiver.sendChatComponent(sender, component)
    }

    private fun validateComponent(component: Component, maxLength: Int): Component {
        if (maxLength <= 0) return component
        return if (gson(component).length > maxLength) {
            Component.text("This chat component is too big.")
        } else {
            component
        }
    }
}