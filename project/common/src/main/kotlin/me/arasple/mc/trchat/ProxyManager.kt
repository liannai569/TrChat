package me.arasple.mc.trchat

import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * @author ItsFlicker
 * @since 2022/6/18 15:19
 */
interface ProxyManager {

    val executor: ExecutorService

    fun sendCommonMessage(recipient: Any, vararg args: String): Future<*>

    fun sendTrChatMessage(recipient: Any, vararg args: String): Future<*>

    fun close() {
    }

}