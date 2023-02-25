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
 * @author ItsFlicker
 * @since 2021/9/12 18:11
 */
@Suppress("Deprecation")
private val jsonParser = JsonParser()
private val reportedErrors = mutableListOf<String>()
val nilUUID = UUID(0, 0)
val papiRegex = "(%)(.+?)(%)|(:)(.+?)(:)|(?!\\{\")((\\{)(.+?)(}))".toRegex()

fun Throwable.print(title: String, printStackTrace: Boolean = true) {
    console().sendMessage("§c[TrChat] §7$title")
    console().sendMessage("§7${javaClass.name}: $localizedMessage")
    if (printStackTrace){
        stackTrace.forEach { console().sendMessage("§8\tat $it") }
        printCause()
    }
}

private fun Throwable.printCause() {
    val cause = cause
    if (cause != null) {
        console().sendMessage("§7Caused by: ${javaClass.name}: ${cause.localizedMessage}")
        cause.stackTrace.forEach { console().sendMessage("§8\tat $it") }
        cause.printCause()
    }
}

fun Throwable.reportOnce(title: String, printStackTrace: Boolean = true) {
    if (title !in reportedErrors) {
        print(title, printStackTrace)
        reportedErrors += title
    }
}

@Suppress("Deprecation")
fun String.parseJson(): JsonElement = jsonParser.parse(this)!!

fun buildMessage(vararg messages: String): List<ByteArray> {
    return MessageBuilder.create(arrayOf(UUID.randomUUID().parseString(), *messages))
}

fun String.toUUID(): UUID = FastUUID.parseUUID(this)

fun UUID.parseString(): String = FastUUID.toString(this)
