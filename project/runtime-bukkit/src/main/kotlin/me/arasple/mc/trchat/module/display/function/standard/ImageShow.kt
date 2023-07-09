package me.arasple.mc.trchat.module.display.function.standard

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.arasple.mc.trchat.module.conf.file.Functions
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.display.function.StandardFunction
import me.arasple.mc.trchat.module.internal.script.Reaction
import me.arasple.mc.trchat.util.CooldownType
import me.arasple.mc.trchat.util.getCooldownLeft
import me.arasple.mc.trchat.util.passPermission
import me.arasple.mc.trchat.util.updateCooldown
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.asList
import taboolib.common.util.resettableLazy
import taboolib.common5.util.decodeBase64
import taboolib.common5.util.encodeBase64
import taboolib.common5.util.parseMillis
import taboolib.module.chat.ComponentText
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.nms.NMSMap
import taboolib.module.nms.buildMap
import taboolib.platform.util.sendLang
import java.net.URL
import java.util.concurrent.CompletableFuture

/**
 * @author ItsFlicker
 * @since 2022/3/12 19:14
 */
@StandardFunction
@PlatformSide([Platform.BUKKIT])
object ImageShow : Function("IMAGE") {

    override val alias = "Image-Show"

    override val reaction by resettableLazy("functions") {
        Functions.conf["General.Image-Show.Action"]?.let { Reaction(it.asList()) }
    }

    @ConfigNode("General.Image-Show.Enabled", "function.yml")
    var enabled = false

    @ConfigNode("General.Image-Show.Permission", "function.yml")
    var permission = "none"

    @ConfigNode("General.Image-Show.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("General.Image-Show.Key", "function.yml")
    val key = ConfigNodeTransfer<String, Regex> { Regex(this) }

    val cache: Cache<String, NMSMap> = CacheBuilder.newBuilder().maximumSize(40).build()
    val tasks = mutableMapOf<String, CompletableFuture<NMSMap>>()

    override fun createVariable(sender: Player, message: String): String {
        return if (!enabled) {
            message
        } else {
            val result = key.get().find(message) ?: return message
            val description = result.groupValues[1]
            val url = result.groupValues[2]
            message.replaceFirst(key.get(), "{{IMAGE:$description;${url.encodeBase64()}}}")
        }
    }

    override fun parseVariable(sender: Player, arg: String): ComponentText? {
        val args = arg.split(";", limit = 2)
        val description = args[0]
        val url = args[1].decodeBase64().decodeToString()
        computeAndCache(url)
        return sender.getComponentFromLang("Function-Image-Show-Format", description, args[1])
    }

    override fun canUse(sender: Player): Boolean {
        return sender.passPermission(permission)
    }

    override fun checkCooldown(sender: Player, message: String): Boolean {
        if (enabled && message.contains(key.get()) && !sender.hasPermission("trchat.bypass.imagecd")) {
            val imageCooldown = sender.getCooldownLeft(CooldownType.IMAGE_SHOW)
            if (imageCooldown > 0) {
                sender.sendLang("Cooldowns-Image-Show", imageCooldown / 1000)
                return false
            } else {
                sender.updateCooldown(CooldownType.IMAGE_SHOW, cooldown.get())
            }
        }
        return true
    }

    fun computeAndCache(url: String) {
        if (url !in tasks) {
            tasks[url] = buildMap(URL(url)) {
                name = "Image"
            }.whenCompleteAsync { map, e ->
                if (e == null && map != null) {
                    cache.put(url, map)
                    tasks -= url
                }
            }
        }
    }

}