package me.arasple.mc.trchat.module.display.channel.obj

/**
 * @author wlys
 * @since 2022/2/6 11:13
 */
class Target(val range: Range, val distance: Int) {

    enum class Range {

        ALL, SINGLE_WORLD, DISTANCE, SELF
    }
}