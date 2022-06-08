package me.arasple.mc.trchat.util

import com.google.gson.JsonParser
import me.arasple.mc.trchat.util.proxy.common.MessageBuilder
import taboolib.common.platform.function.console
import java.text.SimpleDateFormat
import java.util.*

/**
 * Util
 * me.arasple.mc.trchat.util
 *
 * @author wlys
 * @since 2021/9/12 18:11
 */
val jsonParser = JsonParser()

val muteDateFormat = SimpleDateFormat()

fun Throwable.print(title: String, printStackTrace: Boolean = true) {
    console().sendMessage("§c[TrChat] §8$title")
    console().sendMessage("         §8${localizedMessage}")
    if (printStackTrace){
        stackTrace.forEach {
            console().sendMessage("         §8$it")
        }
    }
}

fun String.parseJson() = jsonParser.parse(this)

fun buildMessage(vararg messages: String): List<ByteArray> {
    return MessageBuilder.create(arrayOf(UUID.randomUUID().toString(), *messages))
}