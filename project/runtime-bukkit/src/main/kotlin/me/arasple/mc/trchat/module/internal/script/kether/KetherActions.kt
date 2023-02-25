package me.arasple.mc.trchat.module.internal.script.kether

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.display.channel.Channel
import org.bukkit.entity.Player
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.combinationParser
import taboolib.module.kether.script

fun ScriptFrame.player(): Player {
    return script().sender?.castSafely<Player>() ?: error("No player selected.")
}

@KetherParser(["channel"], namespace = "trchat", shared = true)
internal fun actionChannel() = combinationParser {
    it.group(
        symbol(),
        text().option().defaultsTo(Settings.defaultChannel),
        command("hint", then = bool()).option().defaultsTo(true)
    ).apply(it) { action, channel, hint ->
        now {
            when (action.lowercase()) {
                "join" -> Channel.join(player(), channel, hint)
                "quit", "leave" -> Channel.quit(player())
                else -> error("Unknown channel action: $action")
            }
        }
    }
}

@KetherParser(["filter"], namespace = "trchat", shared = true)
internal fun actionFilter() = combinationParser {
    it.group(
        symbol(),
        text()
    ).apply(it) { action, text ->
        now {
            when (action.lowercase()) {
                "check", "has", "have" -> {
                    TrChat.api().getFilterManager().filter(text).sensitiveWords > 0
                }
                "get", "process" -> {
                    TrChat.api().getFilterManager().filter(text).filtered
                }
                else -> error("Unknown filter action: $action")
            }
        }
    }
}