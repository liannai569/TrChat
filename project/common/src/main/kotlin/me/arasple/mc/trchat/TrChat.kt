package me.arasple.mc.trchat

/**
 * @author ItsFlicker
 * @since 2022/6/18 14:50
 */
object TrChat {

    private var api: TrChatAPI? = null

    fun api(): TrChatAPI {
        return api ?: error("TrChat failed to load!")
    }

    fun register(api: TrChatAPI) {
        this.api = api
    }

}