package me.arasple.mc.trchat.util.color

import me.arasple.mc.trchat.util.Internal
import taboolib.module.chat.colored

/**
 * @author wlys
 * @since 2021/12/12 12:30
 */
@Internal
class DefaultColor(color: String) {

    val type: ColorType

    val color: String

    init {
        this.type = if (Hex.GRADIENT_PATTERN.matcher(color).find()
            || Hex.RAINBOW_PATTERN.matcher(color).find()) {
            ColorType.SPECIAL
        } else {
            ColorType.NORMAL
        }
        this.color = if (type == ColorType.NORMAL) {
            if (color.length == 1) "§$color" else color.colored().parseHex()
        } else {
            color
        }
    }

    enum class ColorType {
        NORMAL, SPECIAL
    }
}