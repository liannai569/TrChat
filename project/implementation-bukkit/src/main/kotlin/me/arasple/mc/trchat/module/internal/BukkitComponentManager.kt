package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.ComponentManager
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.nms.NMS
import me.arasple.mc.trchat.module.conf.file.Filters
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.util.*
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.command.ProxiedCommandSender
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

    override fun filterComponent(component: Component, maxLength: Int): Component {
        val newComponent = if (component is TextComponent && component.content().isNotEmpty()) {
            component.content(TrChat.api().getFilterManager().filter(component.content()).filtered)
        } else {
            component
        }
        return validateComponent(if (newComponent.children().isNotEmpty()) {
            Component.text { builder ->
                newComponent.children().forEach { builder.append(filterComponent(it, -1)) }
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
            is ProxiedCommandSender -> receiver.callee
            is CommandSender -> receiver
            else -> error("Unknown receiver type $receiver.")
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
        val newComponent = if (commandSender is Player && Filters.CONF.getBoolean("Enable.Chat") && commandSender.data.isFilterEnabled) {
            TrChat.api().getComponentManager().filterComponent(component, 32000)
        } else {
            validateComponent(component, 32000)
        }

        if (HookPlugin.getInteractiveChat().sendMessage(commandSender, newComponent)) {
            return
        }
        if (TrChatBukkit.paperEnv) {
            commandSender.sendMessage(identity, newComponent, MessageType.SYSTEM)
        } else {
            if (commandSender is Player) {
                NMS.INSTANCE.sendSystemChatMessage(commandSender, newComponent, identity.uuid())
            } else {
                commandSender.sendMessage(legacy(newComponent))
            }
        }
    }

    override fun sendChatComponent(receiver: Any, component: Component, sender: Any?) {
        val commandSender = when (receiver) {
            is ProxyCommandSender -> receiver.cast()
            is ProxiedCommandSender -> receiver.callee
            is CommandSender -> receiver
            else -> error("Unknown receiver type $receiver.")
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
        val newComponent = if (commandSender is Player && Filters.CONF.getBoolean("Enable.Chat") && commandSender.data.isFilterEnabled) {
            TrChat.api().getComponentManager().filterComponent(component, 32000)
        } else {
            validateComponent(component, 32000)
        }

        if (HookPlugin.getInteractiveChat().sendMessage(commandSender, newComponent)) {
            return
        }
        if (TrChatBukkit.paperEnv) {
            commandSender.sendMessage(identity, newComponent, MessageType.CHAT)
        } else {
            if (commandSender is Player) {
                NMS.INSTANCE.sendPlayerChatMessage(commandSender, newComponent, identity.uuid())
            } else {
                commandSender.sendMessage(legacy(newComponent))
            }
        }
    }

    private fun validateComponent(component: Component, maxLength: Int): Component {
        if (maxLength <= 0) return component
        return if (gson(component).length > maxLength) {
            Component.text("This chat component is too big (> $maxLength).")
        } else {
            component
        }
    }
}