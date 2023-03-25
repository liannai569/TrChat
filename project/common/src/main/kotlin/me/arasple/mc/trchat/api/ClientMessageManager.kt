package me.arasple.mc.trchat.api

import java.util.concurrent.ExecutorService

interface ClientMessageManager {

    val executor: ExecutorService

    fun getPlayerNames(): Map<String, String?>

    fun getExactName(name: String): String

    fun sendDisplayNames()

}