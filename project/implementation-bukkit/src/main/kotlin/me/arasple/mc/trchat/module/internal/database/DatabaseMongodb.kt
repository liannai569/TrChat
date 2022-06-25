package me.arasple.mc.trchat.module.internal.database

import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.util.Internal
import org.bukkit.OfflinePlayer
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.database.bridge.Index
import taboolib.module.database.bridge.createBridgeCollection

/**
 * @author sky
 * @since 2020-08-14 14:46
 */
@Internal
class DatabaseMongodb : Database() {

    val collection = createBridgeCollection(
        Settings.CONF.getString("Database.Mongodb.client")!!,
        Settings.CONF.getString("Database.Mongodb.database")!!,
        Settings.CONF.getString("Database.Mongodb.collection")!!,
        Index.UUID
    )

    override fun pull(player: OfflinePlayer): ConfigurationSection {
        return collection[player.uniqueId.toString()].also {
            if (it.contains("username")) {
                it["username"] = player.name
            }
        }
    }

    override fun push(player: OfflinePlayer) {
        collection.update(player.uniqueId.toString())
    }

    override fun release(player: OfflinePlayer) {
        collection.release(player.uniqueId.toString())
    }
}