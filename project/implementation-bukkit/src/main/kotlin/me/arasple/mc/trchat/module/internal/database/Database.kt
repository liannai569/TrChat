package me.arasple.mc.trchat.module.internal.database

import me.arasple.mc.trchat.util.Internal
import org.bukkit.entity.Player
import taboolib.library.configuration.ConfigurationSection

/**
 * @author sky
 * @since 2020-08-14 14:38
 */
@Internal
abstract class Database {

    abstract fun pull(player: Player): ConfigurationSection

    abstract fun push(player: Player)

    abstract fun release(player: Player)

}