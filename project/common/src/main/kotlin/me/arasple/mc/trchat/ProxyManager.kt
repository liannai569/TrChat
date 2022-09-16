package me.arasple.mc.trchat

import java.util.concurrent.CompletableFuture

/**
 * @author wlys
 * @since 2022/6/18 15:19
 */
interface ProxyManager {

    fun sendCommonMessage(recipient: Any, vararg args: String, async: Boolean = true): CompletableFuture<Boolean>

    fun sendTrChatMessage(recipient: Any, vararg args: String, async: Boolean = true): CompletableFuture<Boolean>

}