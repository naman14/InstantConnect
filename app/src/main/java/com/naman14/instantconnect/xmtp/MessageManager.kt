package com.naman14.instantconnect.xmtp

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.xmtp.android.library.Client
import org.xmtp.android.library.messages.PrivateKeyBuilder

object MessageManager {

    private lateinit var client: Client

    init {
        val account = PrivateKeyBuilder()
        client = Client().create(account = account)
    }

    fun sendMessage(message: String, to: String, callback: (success: Boolean) -> Unit) {
        GlobalScope.launch(Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.printStackTrace()
            callback(false)
        }) {
            val conversation = client.conversations.newConversation(to)
            conversation.send(text = message)
            callback(true)
        }
    }

    suspend fun getMessages(to: String) {
        val conversation = client.conversations.newConversation(to)
        val messages = conversation.messages()
        conversation.streamMessages().collect { message ->
            print("${message.senderAddress}: ${message.body}")
        }
    }
}