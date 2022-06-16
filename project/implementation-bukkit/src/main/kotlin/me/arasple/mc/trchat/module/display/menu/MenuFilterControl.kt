package me.arasple.mc.trchat.module.display.menu

import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.getSession
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.XSound
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.buildItem

/**
 * @author Arasple
 * @date 2019/11/30 11:40
 * Internal Menu For Chat Filter Control
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object MenuFilterControl {

    private val SOUND = XSound.BLOCK_NOTE_BLOCK_PLING

    fun displayFor(player: Player) {
        player.openMenu<Basic>("TrChat Filter") {
            rows(5)
            map(
                "#########",
                "",
                "    A    ",
                "",
                "#########"
            )
            set('#', buildItem(XMaterial.CYAN_STAINED_GLASS_PANE) {
                name = "&3TrChat &bFilter"
                colored()
            })
            set('A', getToggleButton(player))
            onClick(lock = true) {
                if (it.slot == 'A') {
                    player.getSession().setFilter(!player.getSession().isFilterEnabled)
                    displayFor(player)
                }
            }
        }
        SOUND.play(player, 1F, 2F)
    }

    private fun getToggleButton(player: Player): ItemStack {
        return if (player.getSession().isFilterEnabled) {
            buildItem(XMaterial.LIME_STAINED_GLASS_PANE) {
                name = "&3聊天过滤器 &a√"
                lore += listOf("", "&7你已经开启聊天过滤器,", "&7系统将会为您过滤掉聊天", "&7内容中的敏感内容, 祝您游戏愉快", "", "&6▶ &e点击关闭此功能")
                colored()
            }
        } else {
            buildItem(XMaterial.RED_STAINED_GLASS_PANE) {
                name = "&8聊天过滤器 &c×"
                lore += listOf("", "&7你当前已关闭聊天过滤器,", "&7系统将不会为您过滤掉聊天", "&7内容中的敏感内容...", "", "&2▶ &a点击开启此功能")
                colored()
            }
        }
    }
}