package me.arasple.mc.trchat.util

import com.eatthepath.uuid.FastUUID
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import me.arasple.mc.trchat.util.proxy.common.MessageBuilder
import taboolib.common.platform.function.console
import java.util.*

/**
 * Util
 * me.arasple.mc.trchat.util
 *
 * @author wlys
 * @since 2021/9/12 18:11
 */
private val jsonParser = JsonParser()

private val reportedErrors = mutableListOf<String>()

fun Throwable.print(title: String, printStackTrace: Boolean = true) {
    console().sendMessage("§c[TrChat] §8$title")
    console().sendMessage("         §8${localizedMessage}")
    if (printStackTrace){
        stackTrace.forEach {
            console().sendMessage("         §8$it")
        }
    }
}

fun Throwable.reportOnce(title: String, printStackTrace: Boolean = true) {
    if (title !in reportedErrors) {
        print(title, printStackTrace)
        reportedErrors += title
    }
}

fun String.parseJson(): JsonElement = jsonParser.parse(this)!!

fun buildMessage(vararg messages: String): List<ByteArray> {
    return MessageBuilder.create(arrayOf(UUID.randomUUID().parseString(), *messages))
}

fun String.toUUID(): UUID = FastUUID.parseUUID(this)

fun UUID.parseString(): String = FastUUID.toString(this)
