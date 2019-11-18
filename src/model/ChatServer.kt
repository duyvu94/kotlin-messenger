package com.duyvu

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import kotlinx.coroutines.channels.ClosedSendChannelException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class ChatServer {

    suspend fun memberJoin(member: String, socket: WebSocketSession) {

    }


    suspend fun memberLeft(member: String, socket: WebSocketSession) {

    }

    suspend fun sendTo(recipient: String, sender: String, message: String) {

    }

    suspend fun command(sender: String, data: SocketData) {
        println(data.command + " " + data.message);
    }

    private suspend fun broadcast(message: String) {

    }

    private suspend fun broadcast(sender: String, message: String) {

    }

    suspend fun List<WebSocketSession>.send(frame: Frame) {

    }
}