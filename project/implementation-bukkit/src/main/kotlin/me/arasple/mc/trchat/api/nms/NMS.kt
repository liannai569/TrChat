package me.arasple.mc.trchat.api.nms

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.nmsProxy
import java.util.*

/**
 * @author Arasple
 * @date 2019/11/30 11:17
 */
abstract class NMS {

    /**
     * 过滤 IChatBaseComponent 中的敏感词
     *
     * @param iChat 对象
     * @return 过滤后的
     */
    abstract fun filterIChatComponent(iChat: Any?): Any?

    abstract fun filterItem(item: Any?)

    abstract fun filterItemList(items: Any?)

    abstract fun optimizeNBT(itemStack: ItemStack, nbtWhitelist: Array<String> = TellrawJson.whitelistTags): ItemStack

    abstract fun sendChatPreview(player: Player, queryId: Int, query: String)

    abstract fun sendPlayerChatMessage(receiver: Player, component: Component, sender: UUID?)

    abstract fun sendSystemChatMessage(receiver: Player, component: Component, sender: UUID?)

    companion object {

        @JvmStatic
        val INSTANCE = nmsProxy<NMS>()
    }
}
