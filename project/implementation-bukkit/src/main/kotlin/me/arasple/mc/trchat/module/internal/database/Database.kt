package me.arasple.mc.trchat.module.internal.database

import me.arasple.mc.trchat.util.Internal
import org.bukkit.OfflinePlayer
import taboolib.library.configuration.ConfigurationSection

/**
 * @author sky
 * @since 2020-08-14 14:38
 */
@Internal
abstract class Database {

    abstract fun pull(player: OfflinePlayer): ConfigurationSection

    abstract fun push(player: OfflinePlayer)

    abstract fun release(player: OfflinePlayer)

}