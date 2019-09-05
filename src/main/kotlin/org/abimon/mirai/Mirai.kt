package org.abimon.mirai

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.reactor.mono
import org.abimon.mirai.context.BaseMessageContext
import kotlin.contracts.ExperimentalContracts

class Mirai(token: String) {
    companion object {
        @ExperimentalContracts
        @JvmStatic
        fun main(args: Array<String>) {
            Mirai(args[0])
        }
    }

    val client: DiscordClient

    init {
        client = DiscordClientBuilder(token)
            .build()

        client.eventDispatcher
            .on(MessageCreateEvent::class.java)
            .map(::BaseMessageContext)
            .flatMap { msg -> mono { println("[${msg.author?.fulfill(this@Mirai)} / ${msg.guild}] ${msg.content}") } }
            .subscribe()

        client.login()
            .block()
    }
}