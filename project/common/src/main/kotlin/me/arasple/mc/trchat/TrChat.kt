package me.arasple.mc.trchat

/**
 * @author wlys
 * @since 2022/6/18 14:50
 */
object TrChat {

    private var api: TrChatAPI? = null

    fun api(): TrChatAPI {
        return api!!
    }

    fun register(api: TrChatAPI) {
        this.api = api
    }

}