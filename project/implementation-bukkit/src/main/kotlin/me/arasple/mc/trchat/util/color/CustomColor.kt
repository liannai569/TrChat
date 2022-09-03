package me.arasple.mc.trchat.util.color

/**
 * @author wlys
 * @since 2021/12/12 12:30
 */
class CustomColor(val type: ColorType, val color: String) {

    enum class ColorType {

        NORMAL, SPECIAL
    }

    companion object {

        private val caches = mutableMapOf<String, CustomColor>()

        fun get(string: String): CustomColor {
            return caches.computeIfAbsent(string) {
                val type = if (Hex.GRADIENT_PATTERN.matcher(it).find() || Hex.RAINBOW_PATTERN.matcher(it).find()) {
                    ColorType.SPECIAL
                } else {
                    ColorType.NORMAL
                }
                val color = if (type == ColorType.NORMAL) {
                    if (it.length == 1) "§$it" else it.parseHex().parseLegacy()
                } else {
                    it
                }
                CustomColor(type, color)
            }
        }
    }
}