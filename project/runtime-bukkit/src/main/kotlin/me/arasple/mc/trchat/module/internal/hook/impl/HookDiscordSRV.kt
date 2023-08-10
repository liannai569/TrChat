package me.arasple.mc.trchat.module.internal.hook.impl

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreBroadcastEvent
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent
import me.arasple.mc.trchat.module.internal.hook.HookAbstract
import me.arasple.mc.trchat.util.session
import org.bukkit.entity.Player

class HookDiscordSRV : HookAbstract() {

    override fun init() {
        if (!isHooked) return
        DiscordSRV.api.subscribe(object {
            @Subscribe
            fun onChatPreProcess(e: GameChatMessagePreProcessEvent) {
                if (e.player.session.lastChannel?.settings?.sendToDiscord == false) {
                    e.isCancelled = true
                }
            }
            @Subscribe
            fun onMessagePreBroadcast(e: DiscordGuildMessagePreBroadcastEvent) {
                e.recipients.removeIf {
                    (it as? Player)?.session?.getChannel()?.settings?.receiveFromDiscord == false
                }
            }
        })
    }

}