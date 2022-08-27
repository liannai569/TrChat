package me.arasple.mc.trchat.api.event

import me.arasple.mc.trchat.module.display.function.CustomFunction
import taboolib.platform.type.BukkitProxyEvent

class TrChatReloadEvent {

    class Function(val customFunctions: List<CustomFunction>) : BukkitProxyEvent()

    class Channel : BukkitProxyEvent()

}