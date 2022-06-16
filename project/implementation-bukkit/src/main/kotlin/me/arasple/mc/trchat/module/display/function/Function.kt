package me.arasple.mc.trchat.module.display.function

import me.arasple.mc.trchat.module.display.format.JsonComponent
import me.arasple.mc.trchat.module.internal.script.Condition
import taboolib.common.util.replaceWithOrder
import java.util.regex.Pattern

/**
 * @author wlys
 * @since 2021/12/12 11:41
 */
class Function(
    val id: String,
    val condition: Condition?,
    val priority: Int,
    val regex: Regex,
    val filterTextPattern: Pattern?,
    val displayJson: JsonComponent,
    val action: String?
) {

    companion object {

        val functions = mutableListOf<Function>()

        fun String.replaceRegex(regex: Regex, textPattern: Pattern?, replacement: String): String {
            var string = this
            regex.findAll(string).forEach {
                val str = it.value
                val matcher = textPattern?.matcher(str)
                val rep = replacement.replaceWithOrder(
                    if (matcher != null && matcher.find()) {
                        matcher.group()
                    } else {
                        str
                    })
                string = string.replace(str, rep)
            }
            return string
        }
    }
}