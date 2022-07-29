package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.ComponentManager
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.data
import me.arasple.mc.trchat.util.gson
import me.arasple.mc.trchat.util.toUUID
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.common.platform.*
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

    override fun sendSystemComponent(receiver: Any, component: Component, sender: Any?) {
        val commandSender = when (receiver) {
            is ProxyCommandSender -> receiver.cast()
            is CommandSender -> receiver
            else -> error("Unsupported receiver type $receiver.")
        }
        val identity = when (sender) {
            is ProxyPlayer -> sender.uniqueId
            is Entity -> sender.uniqueId
            is String -> sender.toUUID()
            is UUID -> sender
            else -> null
        }?.let { Identity.identity(it) } ?: Identity.nil()

        if (commandSender is Player && commandSender.data.ignored.contains(identity.uuid())) {
            return
        }

        if (HookPlugin.getInteractiveChat().sendMessage(commandSender, component)) {
            return
        }
        if (TrChatBukkit.paperEnv) {
            commandSender.sendMessage(component, MessageType.SYSTEM)
        } else {
            getAudienceProvider().sender(commandSender).sendMessage(component, MessageType.SYSTEM)
        }
    }

    override fun sendChatComponent(receiver: Any, component: Component, sender: Any?) {
        val commandSender = when (receiver) {
            is ProxyCommandSender -> receiver.cast()
            is CommandSender -> receiver
            else -> error("Unsupported receiver type $receiver.")
        }
        val identity = when (sender) {
            is ProxyPlayer -> sender.uniqueId
            is Entity -> sender.uniqueId
            is String -> sender.toUUID()
            is UUID -> sender
            else -> null
        }?.let { Identity.identity(it) } ?: Identity.nil()

        if (commandSender is Player && commandSender.data.ignored.contains(identity.uuid())) {
            return
        }

        if (HookPlugin.getInteractiveChat().sendMessage(commandSender, component)) {
            return
        }
        if (TrChatBukkit.paperEnv) {
            commandSender.sendMessage(identity, component, MessageType.CHAT)
        } else {
            getAudienceProvider().sender(commandSender).sendMessage(identity, component, MessageType.CHAT)
        }
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