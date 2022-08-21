package me.arasple.mc.trchat.module.conf

import me.arasple.mc.trchat.module.conf.file.Functions
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.display.channel.PrivateChannel
import me.arasple.mc.trchat.module.display.channel.obj.ChannelBindings
import me.arasple.mc.trchat.module.display.channel.obj.ChannelEvents
import me.arasple.mc.trchat.module.display.channel.obj.ChannelSettings
import me.arasple.mc.trchat.module.display.channel.obj.Target
import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.display.format.Group
import me.arasple.mc.trchat.module.display.format.JsonComponent
import me.arasple.mc.trchat.module.display.format.MsgComponent
import me.arasple.mc.trchat.module.display.format.obj.Style
import me.arasple.mc.trchat.module.display.format.obj.Text
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.internal.proxy.BukkitProxyManager
import me.arasple.mc.trchat.module.internal.script.Reaction
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.color.CustomColor
import me.arasple.mc.trchat.util.print
import me.arasple.mc.trchat.util.toCondition
import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.io.newFile
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.util.asList
import taboolib.common.util.orNull
import taboolib.common.util.unsafeLazy
import taboolib.common5.Coerce
import taboolib.common5.FileWatcher
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.util.getMap
import taboolib.module.lang.sendLang
import taboolib.platform.util.onlinePlayers
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * @author wlys
 * @since 2021/12/12 13:45
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object Loader {

    private val folder by unsafeLazy {
        val folder = File(getDataFolder(), "channels")

        if (!folder.exists()) {
            arrayOf(
                "Normal.yml",
                "Global.yml",
                "Staff.yml",
                "Private.yml"
            ).forEach { releaseResourceFile("channels/$it", replace = true) }
            newFile(File(getDataFolder(), "data"), folder = true)
        }

        folder
    }

    fun loadChannels(sender: ProxyCommandSender) {
        measureTimeMillis { loadChannels() }.let {
            sender.sendLang("Plugin-Loaded-Channels", Channel.channels.size, it)
        }
    }

    fun loadChannels(): Int {
        Channel.channels.values.forEach { it.unregister() }
        Channel.channels.clear()

        if (onlinePlayers.isNotEmpty()) {
            BukkitProxyManager.sendTrChatMessage(onlinePlayers.iterator().next(), "FetchProxyChannels")
        }

        filterChannelFiles(folder).forEach {
            if (FileWatcher.INSTANCE.hasListener(it)) {
                try {
                    loadChannel(it.nameWithoutExtension, YamlConfiguration.loadConfiguration(it)).let { channel ->
                        Channel.channels[channel.id] = channel
                    }
                } catch (t: Throwable) {
                    t.print("Channel file ${it.name} loaded failed!")
                }
            } else {
                FileWatcher.INSTANCE.addSimpleListener(it, {
                    try {
                        loadChannel(it.nameWithoutExtension, YamlConfiguration.loadConfiguration(it)).let { channel ->
                            Channel.channels[channel.id] = channel
                        }
                    } catch (t: Throwable) {
                        t.print("Channel file ${it.name} loaded failed!")
                    }
                }, true)
            }
        }

        return Channel.channels.size
    }

    fun loadChannel(id: String, conf: YamlConfiguration): Channel {
        Channel.channels[id]?.let {
            it.unregister()
            Channel.channels.remove(it.id)
        }

        val settings = conf.getConfigurationSection("Options")!!.let { section ->
            val joinPermission = section.getString("Join-Permission")
            val speakCondition = section.getString("Speak-Condition")?.toCondition()
            val target = section.getString("Target", "ALL")!!.uppercase().split(";").let {
                val distance = it.getOrNull(1)?.toInt() ?: -1
                Target(Target.Range.valueOf(it[0]), distance)
            }
            val autoJoin = section.getBoolean("Auto-Join", true)
            val proxy = section.getBoolean("Proxy", false)
            val doubleTransfer = section.getBoolean("Double-Transfer", true)
            val ports = section.getString("Ports")?.split(";")?.map { it.toInt() }
            val disabledFunctions = section.getStringList("Disabled-Functions")
            val filterBeforeSending = section.getBoolean("Filter-Before-Sending", false)
            ChannelSettings(joinPermission, speakCondition, target, autoJoin, proxy, doubleTransfer, ports, disabledFunctions, filterBeforeSending)
        }
        val private = conf.getBoolean("Options.Private", false)

        val bindings = conf.getConfigurationSection("Bindings")?.let {
            val prefix = if (!private) it.getStringList("Prefix") else null
            val command = it.getStringList("Command")
            ChannelBindings(prefix, command)
        } ?: ChannelBindings(null, null)

        val events = conf.getConfigurationSection("Events")?.let {
            val process = it["Process"]?.asList() ?: emptyList()
            val send = it["Send"]?.asList() ?: emptyList()
            ChannelEvents(Reaction(process), Reaction(send))
        } ?: ChannelEvents(null, null)

        if (private) {
            val sender = conf.getMapList("Sender").map { map ->
                val condition = map["condition"]?.toString()?.toCondition()
                val priority = Coerce.asInteger(map["priority"]).orNull() ?: 100
                val prefix = parseGroups(map["prefix"] as LinkedHashMap<*, *>)
                val msg = parseMsg(map["msg"] as LinkedHashMap<*, *>)
                val suffix = parseGroups(map["suffix"] as? LinkedHashMap<*, *>)
                Format(condition, priority, prefix, msg, suffix)
            }.sortedBy { it.priority }
            val receiver = conf.getMapList("Receiver").map { map ->
                val condition = map["condition"]?.toString()?.toCondition()
                val priority = Coerce.asInteger(map["priority"]).orNull() ?: 100
                val prefix = parseGroups(map["prefix"] as LinkedHashMap<*, *>)
                val msg = parseMsg(map["msg"] as LinkedHashMap<*, *>)
                val suffix = parseGroups(map["suffix"] as? LinkedHashMap<*, *>)
                Format(condition, priority, prefix, msg, suffix)
            }.sortedBy { it.priority }

            return PrivateChannel(id, settings, bindings, events, sender, receiver)
        } else {
            val formats = conf.getMapList("Formats").map { map ->
                val condition = map["condition"]?.toString()?.toCondition()
                val priority = Coerce.asInteger(map["priority"]).orNull() ?: 100
                val prefix = parseGroups(map["prefix"] as LinkedHashMap<*, *>)
                val msg = parseMsg(map["msg"] as LinkedHashMap<*, *>)
                val suffix = parseGroups(map["suffix"] as? LinkedHashMap<*, *>)
                Format(condition, priority, prefix, msg, suffix)
            }.sortedBy { it.priority }
            val console = conf.getMapList("Console").firstOrNull()?.let { map ->
                val prefix = parseGroups(map["prefix"] as LinkedHashMap<*, *>)
                val msg = parseMsg(map["msg"] as LinkedHashMap<*, *>)
                val suffix = parseGroups(map["suffix"] as? LinkedHashMap<*, *>)
                Format(null, 100, prefix, msg, suffix)
            }
            return Channel(id, settings, bindings, events, formats, console)
        }
    }

    fun loadFunctions(sender: ProxyCommandSender) {
        measureTimeMillis { loadFunctions() }.let {
            sender.sendLang("Plugin-Loaded-Functions", Function.functions.size + 3, it)
        }
    }

    fun loadFunctions() {
        Function.functions.clear()

        val customs = Functions.CONF.getMap<String, ConfigurationSection>("Custom")
        val functions = customs.map { (id, map) ->
            val condition = map.getString("condition")?.toCondition()
            val priority = map.getInt("priority", 100)
            val regex = map.getString("pattern")!!.toRegex()
            val filterTextRegex = map.getString("text-filter")?.toRegex()
            val displayJson = parseJSON(map.getConfigurationSection("display")!!.toMap())
            val action = map["action"]?.toString()

            Function(id, condition, priority, regex, filterTextRegex, displayJson, action)
        }.sortedBy { it.priority }

        Function.functions.addAll(functions)
    }

    private fun parseGroups(map: LinkedHashMap<*, *>?): Map<String, List<Group>> {
        map ?: return emptyMap()
        return map.map { (id, content) ->
            id as String
            when (content) {
                is Map<*, *> -> {
                    val condition = content["condition"]?.toString()?.toCondition()
                    id to listOf(Group(condition, 100, parseJSON(content)))
                }
                is List<*> -> {
                    id to content.map {
                        it as LinkedHashMap<*, *>
                        val condition = it["condition"]?.toString()?.toCondition()
                        val priority = Coerce.asInteger(map["priority"]).orNull() ?: 100
                        Group(condition, priority, parseJSON(it))
                    }.sortedBy { it.priority }
                }
                else -> error("Unexpected group: $content")
            }
        }.toMap()
    }

    private fun parseJSON(content: Map<*, *>): JsonComponent {
        val text = Property.serialize(content["text"] ?: "null").map { Text(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val style = mutableListOf<Style?>()
        style += content["hover"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Hover.Text(it) }
        style += content["suggest"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Click.Suggest(it) }
        style += content["command"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Click.Command(it) }
        style += content["url"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Click.Url(it) }
        style += content["copy"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Click.Copy(it) }
        style += content["file"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Click.File(it) }
        style += content["insertion"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Insertion(it) }
        style += content["font"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Font(it) }
        return JsonComponent(text, style.filterNotNull())
    }

    private fun parseMsg(content: Map<*, *>): MsgComponent {
        val defaultColor = content["default-color"]!!.serialize().map { CustomColor.get(it.first) to it.second[Property.CONDITION]?.toCondition() }
        val style = mutableListOf<Style?>()
        style += content["hover"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Hover.Text(it) }
        style += content["suggest"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Click.Suggest(it) }
        style += content["command"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Click.Command(it) }
        style += content["url"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Click.Url(it) }
        style += content["copy"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Click.Copy(it) }
        style += content["file"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Click.File(it) }
        style += content["insertion"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Insertion(it) }
        style += content["font"]?.serialize()?.map { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Style.Font(it) }
        return MsgComponent(defaultColor, style.filterNotNull())
    }

    private fun filterChannelFiles(file: File): List<File> {
        return mutableListOf<File>().apply {
            if (file.isDirectory) {
                file.listFiles()?.forEach {
                    addAll(filterChannelFiles(it))
                }
            } else if (file.extension.equals("yml", true)) {
                add(file)
            }
        }
    }

    private fun Any.serialize(): List<Pair<String, Map<Property, String>>> {
        return Property.serialize(this)
    }
}