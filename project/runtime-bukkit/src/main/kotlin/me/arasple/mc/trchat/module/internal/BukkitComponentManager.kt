package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.ComponentManager
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.nms.NMS
import me.arasple.mc.trchat.module.conf.file.Filters
import me.arasple.mc.trchat.util.data
import me.arasple.mc.trchat.util.toUUID
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.command.ProxiedCommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.common.platform.*
import taboolib.common.platform.function.adaptCommandSender
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.chat.impl.DefaultComponent
import java.util.*

/**
 * @author ItsFlicker
 * @since 2022/6/8 12:56
 */
@PlatformSide([Platform.BUKKIT])
object BukkitComponentManager : ComponentManager {

    init {
        PlatformFactory.registerAPI<ComponentManager>(this)
    }

    override fun filterComponent(component: ComponentText, maxLength: Int): ComponentText {
        return if (Filters.CONF.getBoolean("Enable.Chat")) {
            val baseComponents = ComponentSerializer.parse(component.toRawMessage())
            validateComponent(DefaultComponent(baseComponents.map { filterComponent(it) }), maxLength)
        } else {
            validateComponent(component, maxLength)
        }
    }

    override fun sendComponent(receiver: Any, component: ComponentText, sender: Any?) {
        val commandSender = when (receiver) {
            is ProxyCommandSender -> receiver.cast()
            is ProxiedCommandSender -> receiver.callee
            is CommandSender -> receiver
            else -> error("Unknown receiver type $receiver.")
        }
        val uuid = when (sender) {
            is ProxyPlayer -> sender.uniqueId
            is Entity -> sender.uniqueId
            is String -> sender.toUUID()
            is UUID -> sender
            else -> null
        }

        if (commandSender is Player && commandSender.data.ignored.contains(uuid)) {
            return
        }
        val newComponent = if (commandSender is Player && commandSender.data.isFilterEnabled) {
            filterComponent(component, 32000)
        } else {
            validateComponent(component, 32000)
        }

        if (commandSender is Player) {
            NMS.instance.sendMessage(commandSender, newComponent, uuid)
        } else {
            newComponent.sendTo(adaptCommandSender(commandSender))
        }
    }

    private fun validateComponent(component: ComponentText, maxLength: Int): ComponentText {
        if (maxLength <= 0) return component
        return if (component.toRawMessage().length > maxLength) {
            Components.text("This chat component is too big to show (> $maxLength).")
        } else {
            component
        }
    }

    private fun filterComponent(component: BaseComponent): BaseComponent {
        if (component is TextComponent && component.text.isNotEmpty()) {
            component.text = TrChat.api().getFilterManager().filter(component.text).filtered
        }
        if (!component.extra.isNullOrEmpty()) {
            component.extra = component.extra.map { filterComponent(it) }
        }
        return component
    }
}