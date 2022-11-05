package me.arasple.mc.trchat.module.internal.redis

import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.util.passPermission
import me.arasple.mc.trchat.util.sendComponent
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.expansion.AlkaidRedis
import taboolib.expansion.SingleRedisConnection
import taboolib.expansion.SingleRedisConnector
import taboolib.expansion.fromConfig
import taboolib.module.configuration.ConfigNode
import taboolib.module.lang.sendLang
import taboolib.platform.util.onlinePlayers

@PlatformSide([Platform.BUKKIT])
object RedisManager {

    private var connector: SingleRedisConnector? = null
    var connection: SingleRedisConnection? = null

    @ConfigNode("Redis.enabled", "settings.yml")
    var enabled = false

    operator fun invoke(default: Boolean = true): SingleRedisConnection? {
        if (!enabled) {
            return null
        }
        if (connector == null) {
            connector = AlkaidRedis.create().apply {
                fromConfig(Settings.CONF.getConfigurationSection("Redis")!!)
            }
            console().sendLang("Plugin-Proxy-Supported", "Redis")
        }
        connection?.close()
        connection = connector!!.connect().connection()
        if (default) {
            init(connection!!)
        }
        return connection
    }

    fun init(connection: SingleRedisConnection) {
        connection.subscribe(*Settings.CONF.getStringList("Redis.subscribe").toTypedArray()) {
            val message = get<RedisChatMessage>(false)
            if (message.target == null) {
                if (message.permission == null) {
                    onlinePlayers.forEach { it.sendComponent(message.sender, message.component) }
                } else {
                    onlinePlayers
                        .filter { it.passPermission(message.permission) }
                        .forEach { it.sendComponent(message.sender, message.component) }
                }
                console().sendComponent(message.sender, message.component)
            } else {
                Bukkit.getPlayer(message.target)?.sendComponent(message.sender, message.component)
            }
        }
    }

    fun sendMessage(channel: String, message: RedisChatMessage) {
        if (enabled) {
            (connection ?: RedisManager())!!.publish(channel, message)
        }
    }

    @Awake(LifeCycle.DISABLE)
    fun close() {
        connection?.close()
    }

}