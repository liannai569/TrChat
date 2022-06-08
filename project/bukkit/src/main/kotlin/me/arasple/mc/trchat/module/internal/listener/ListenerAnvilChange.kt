package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.api.config.Filters
import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.color.MessageColors
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.isAir
import taboolib.platform.util.modifyMeta

/**
 * @author Arasple, wlys
 * @date 2019/8/15 21:18
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object ListenerAnvilChange {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onAnvilCraft(e: PrepareAnvilEvent) {
        val p = e.view.player
        val result = e.result

        if (e.inventory.type != InventoryType.ANVIL || result.isAir()) {
            return
        }
        result!!.modifyMeta<ItemMeta> {
            if (!hasDisplayName()) {
                return@modifyMeta
            }
            if (Filters.CONF.getBoolean("Enable.Anvil")) {
                setDisplayName(TrChatAPI.filterString(p, displayName).filtered)
            }
            if (Settings.CONF.getBoolean("Color.Anvil")) {
                setDisplayName(MessageColors.replaceWithPermission(p, displayName, MessageColors.Type.ANVIL))
            }
        }
        e.result = result
    }
}