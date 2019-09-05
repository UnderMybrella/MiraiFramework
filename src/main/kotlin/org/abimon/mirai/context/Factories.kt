package org.abimon.mirai.context

import discord4j.core.`object`.entity.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.abimon.mirai.Mirai

fun channelContextFor(channel: Channel): ChannelContext? = null

open class CachedSuspendValue<T>(val block: suspend (mirai: Mirai) -> T?) {
    val mutex: Mutex = Mutex()
    var value: T? = null
    var retry: Boolean = true

    suspend fun fulfill(mirai: Mirai): T? = mutex.withLock {
        if (retry) {
            value = block(mirai)
            retry = false
        }

        value
    }
}

//inline fun <T> cachedSuspendValue(block: suspend (mirai: Mirai) -> T?) = CachedSuspendValue(block)