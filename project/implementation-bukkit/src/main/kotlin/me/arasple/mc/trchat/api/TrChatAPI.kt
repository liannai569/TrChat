package me.arasple.mc.trchat.api

import me.arasple.mc.trchat.module.display.filter.ChatFilter.filter
import me.arasple.mc.trchat.module.display.filter.processer.FilteredObject
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.sendProcessedMessage
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.platform.function.adaptPlayer
import taboolib.common5.mirrorNow
import taboolib.library.kether.LocalizedException
import taboolib.module.kether.KetherShell
import taboolib.module.nms.nmsClass
import taboolib.module.nms.obcClass
import taboolib.platform.util.isAir
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author Arasple, wlys
 * @date 2019/8/18 0:18
 */
object TrChatAPI {

    /**
     * 根据玩家的权限 (trchat.bypass.filter)，过滤字符串
     *
     * @param player 玩家
     * @param string 字符串
     * @param execute 是否真的过滤
     * @return 过滤后的
     */
    @JvmStatic
    @JvmOverloads
    fun filterString(player: HumanEntity, string: String, execute: Boolean = true): FilteredObject {
        return if (execute) filter(string, !player.hasPermission("trchat.bypass.filter")) else FilteredObject(string, 0)
    }

    /**
     * 过滤物品的名字和Lore
     *
     * @param itemStack 物品
     */
    @JvmStatic
    fun filterItemStack(itemStack: ItemStack) {
        if (itemStack.isAir()) {
            return
        }
        itemStack.modifyMeta<ItemMeta> {
            if (hasDisplayName()) {
                setDisplayName(filter(displayName).filtered)
            }
            modifyLore {
                if (isNotEmpty()) {
                    replaceAll { filter(it).filtered }
                }
            }
        }
    }

    /**
     * 根据权限替换颜色
     *
     * @param player 玩家
     * @param string 内容
     * @param type 替换类型
     */
    @JvmOverloads
    @JvmStatic
    fun replaceColorWithPermission(player: HumanEntity, string: String, type: MessageColors.Type = MessageColors.Type.DEFAULT): String {
        return MessageColors.replaceWithPermission(player, string, type)
    }

    /**
     * 发送Component
     *
     * @param receiver 接收者
     * @param component 内容
     * @param sender 发送者UUID
     */
    @JvmOverloads
    @JvmStatic
    fun sendComponent(receiver: CommandSender, component: Component, sender: UUID = UUID.randomUUID()) {
        receiver.sendProcessedMessage(sender, component)
    }

    @JvmStatic
    fun eval(player: Player, script: String): CompletableFuture<Any?> {
        return mirrorNow("Handler:Script:Evaluation") {
            return@mirrorNow try {
                KetherShell.eval(script, namespace = listOf("trchat", "trmenu", "trhologram")) {
                    sender = adaptPlayer(player)
                }
            } catch (e: LocalizedException) {
                println("§c[TrChat] §8Unexpected exception while parsing kether shell:")
                e.localizedMessage.split("\n").forEach {
                    println("         §8$it")
                }
                CompletableFuture.completedFuture(false)
            }
        }
    }

    val classCraftItemStack by lazy {
        obcClass("inventory.CraftItemStack")
    }

    val classChatSerializer by lazy {
        nmsClass("IChatBaseComponent\$ChatSerializer")
    }
}