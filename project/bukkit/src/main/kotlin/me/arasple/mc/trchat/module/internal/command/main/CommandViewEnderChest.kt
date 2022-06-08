package me.arasple.mc.trchat.module.internal.command.main

import me.arasple.mc.trchat.module.display.function.EnderChestShow
import me.arasple.mc.trchat.util.Internal
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.expansion.createHelper
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * @author wlys
 * @since 2022/2/6 15:01
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object CommandViewEnderChest {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("view-enderchest", permissionDefault = PermissionDefault.TRUE) {
            dynamic("enderchest") {
                execute<Player> { sender, _, argument ->
                    EnderChestShow.cache.getIfPresent(argument)?.let {
                        sender.openInventory(it)
                    } ?: kotlin.run {
                        sender.sendLang("EnderChest-Show-Unavailable")
                    }
                }
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
            incorrectCommand { _, _, _, _ ->
                createHelper()
            }
        }
    }
}