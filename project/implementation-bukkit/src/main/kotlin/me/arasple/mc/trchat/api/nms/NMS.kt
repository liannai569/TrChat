package me.arasple.mc.trchat.api.nms

import org.bukkit.inventory.ItemStack
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.nmsProxy

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

    companion object {

        val INSTANCE by lazy {
            nmsProxy<NMS>()
        }
    }
}
