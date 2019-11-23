package com.duyvu

import com.duyvu.dao.DAOFacade
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import kotlinx.coroutines.channels.ClosedSendChannelException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class SocketServer {

    private val onLineUsers = ConcurrentHashMap<String, MutableList<WebSocketSession>>()

    suspend fun userOnline(userId: String, socket: WebSocketSession) {
        val list = onLineUsers.computeIfAbsent(userId) { CopyOnWriteArrayList<WebSocketSession>() }
        list.add(socket)
    }


    suspend fun userOffline(userId: String, socket: WebSocketSession) {
        val connections = onLineUsers[userId]
        connections?.remove(socket)
    }

    suspend fun sendTo(recipient: String, sender: String, message: String) {

    }

    suspend fun command(sender: String, data: SocketData, dao: DAOFacade) {
        println(data.command + " " + data.message);
        when (data.command) {
            "client-add-friend" -> addFriend(sender, data.message, dao)
        }
    }

    private suspend fun addFriend(fromUserId: String, email : String?, dao: DAOFacade) {
        if (email != null){
            val toUserId = dao.userByEmail(email)?.userId
            val userEmail = dao.user(fromUserId)?.email

            if (fromUserId != toUserId && dao.createRelationship(fromUserId, email)){
                onLineUsers[fromUserId]?.send(Frame.Text("{\"command\": \"server-add-friend\" , \"message\" : \"$email\"}"));
                onLineUsers[toUserId]?.send(Frame.Text("{\"command\": \"server-friend-request\" , \"message\" : \"$userEmail\"}"));
            }
        }
        else
            onLineUsers[fromUserId]?.send(Frame.Text("{\"command\": \"server-add-friend\" , \"message\" : \"failure\"}"));
    }

    private suspend fun broadcast(message: String) {

    }

    private suspend fun broadcast(sender: String, message: String) {

    }

    private suspend fun List<WebSocketSession>.send(frame: Frame) {
        forEach {
            try {
                it.send(frame.copy())
            } catch (t: Throwable) {
                try {
                    it.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                } catch (ignore: ClosedSendChannelException) {
                }
            }
        }
    }
}