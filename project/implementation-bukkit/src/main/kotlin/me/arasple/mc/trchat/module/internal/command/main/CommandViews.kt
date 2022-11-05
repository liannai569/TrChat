package me.arasple.mc.trchat.module.internal.command.main

import me.arasple.mc.trchat.module.display.function.standard.EnderChestShow
import me.arasple.mc.trchat.module.display.function.standard.ImageShow
import me.arasple.mc.trchat.module.display.function.standard.InventoryShow
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common5.util.decodeBase64
import taboolib.expansion.createHelper
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * @author wlys
 * @since 2022/2/6 15:01
 */
@PlatformSide([Platform.BUKKIT])
object CommandViews {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("view-inventory", permissionDefault = PermissionDefault.TRUE) {
            dynamic("inventory") {
                execute<Player> { sender, _, argument ->
                    InventoryShow.cache.getIfPresent(argument)?.let {
                        sender.openInventory(it)
                    } ?: kotlin.run {
                        sender.sendLang("Function-Inventory-Show-Unavailable")
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
        command("view-enderchest", permissionDefault = PermissionDefault.TRUE) {
            dynamic("enderchest") {
                execute<Player> { sender, _, argument ->
                    EnderChestShow.cache.getIfPresent(argument)?.let {
                        sender.openInventory(it)
                    } ?: kotlin.run {
                        sender.sendLang("Function-EnderChest-Show-Unavailable")
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
        command("view-image", permissionDefault = PermissionDefault.TRUE) {
            dynamic("image") {
                execute<Player> { sender, _, argument ->
                    val url = argument.decodeBase64().decodeToString()
                    ImageShow.cache.getIfPresent(url)?.sendTo(sender)
                        ?: kotlin.run {
                            ImageShow.computeAndCache(url)
                            sender.sendLang("Function-Image-Show-Unavailable")
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