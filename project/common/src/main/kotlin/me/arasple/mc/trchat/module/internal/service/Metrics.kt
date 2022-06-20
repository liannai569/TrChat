package me.arasple.mc.trchat.module.internal.service

import taboolib.common.platform.function.pluginVersion
import taboolib.common.platform.function.runningPlatform
import taboolib.module.metrics.Metrics
import taboolib.module.metrics.charts.SingleLineChart

/**
 * @author Arasple
 */
object Metrics {

    private lateinit var metrics: Metrics

    private val counts = intArrayOf(0, 0)

    fun init(serviceId: Int) {
        metrics = Metrics(serviceId, pluginVersion, runningPlatform).apply {
            // 聊天次数统计
            addCustomChart(SingleLineChart("chat_counts") {
                val i = counts[0]
                counts[0] = 0
                i
            })
            // 敏感词过滤器启用统计
            addCustomChart(SingleLineChart("filter_counts") {
                val i = counts[1]
                counts[1] = 0
                i
            })
        }
    }

    fun increase(index: Int, value: Int = 1) {
        if (counts[index] < Int.MAX_VALUE) {
            counts[index] += value
        }
    }
}