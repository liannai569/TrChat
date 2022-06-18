package me.arasple.mc.trchat

/**
 * @author wlys
 * @since 2022/6/18 15:19
 */
interface ProxyManager {

    fun sendCommonMessage(recipient: Any, vararg args: String, async: Boolean = true): Boolean

    fun sendTrChatMessage(recipient: Any, vararg args: String, async: Boolean = true): Boolean

    fun getPlayers(): List<String>

}