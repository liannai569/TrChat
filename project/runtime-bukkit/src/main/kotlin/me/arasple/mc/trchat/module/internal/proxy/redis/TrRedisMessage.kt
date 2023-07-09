package me.arasple.mc.trchat.module.internal.proxy.redis

import me.arasple.mc.trchat.util.ArrayLikeConverter
import taboolib.library.configuration.Conversion

class TrRedisMessage(
    @Conversion(ArrayLikeConverter::class) val data: Array<String>
)