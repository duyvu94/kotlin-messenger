package com.duyvu.model

import com.duyvu.SocketData
import com.duyvu.dao.DAOFacade
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import kotlinx.coroutines.channels.ClosedSendChannelException
import org.joda.time.DateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

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

    suspend fun command(sender: String, data: SocketData, dao: DAOFacade) {
        println(data.command + " " + data.message);
        when (data.command) {
            "client-add-friend" -> addFriend(sender, data.message, dao)
            "client-accept-friend" -> acceptFriend(sender, data.message, dao)
            "client-send-message" -> sendMessage(sender, data.message, data.extraMessage, dao)
            "client-chat-history" -> chatHistory(sender, data.message, dao)
        }
    }

    private suspend fun chatHistory(fromUserId: String, toUserId: String?, dao: DAOFacade){
        if (toUserId != null ){
            dao.messageList(fromUserId, toUserId).forEach{
                if (it.fromUserId == fromUserId)
                    onLineUsers[fromUserId]?.send(
                        Frame.Text("{\"command\": \"server-send-message\" , \"message\" : \"$toUserId\", \"extraMessage\" : \"${it.content}\", \"date\" : \"${it.date}\" }"));
                else
                    onLineUsers[fromUserId]?.send(
                        Frame.Text("{\"command\": \"server-receive-message\" , \"message\" : \"$toUserId\", \"extraMessage\" : \"${it.content}\", \"date\" : \"${it.date}\" }"));

            }
        }
    }

    private suspend fun sendMessage(fromUserId: String, toUserId: String?, message: String?, dao: DAOFacade){
        val date = DateTime.now()
        if (toUserId != null && message != null){
            if (dao.sendMessage(fromUserId, toUserId, message, date)){
                onLineUsers[fromUserId]?.send(Frame.Text("{\"command\": \"server-send-message\" , \"message\" : \"$toUserId\", \"extraMessage\" : \"$message\", \"date\" : \"$date\" }"));
                onLineUsers[toUserId]?.send(Frame.Text("{\"command\": \"server-receive-message\" , \"message\" : \"$fromUserId\", \"extraMessage\" : \"$message\", \"date\" : \"$date\" }"));
            }
            else
                onLineUsers[fromUserId]?.send(Frame.Text("{\"command\": \"server-send-message\" , \"message\" : \"failure\"}"));
        }
    }

    private suspend fun addFriend(fromUserId: String, email : String?, dao: DAOFacade) {
        if (email != null){
            val toUserId = dao.userByEmail(email)?.userId
            val userEmail = dao.user(fromUserId)?.email

            if (toUserId != null && fromUserId != toUserId && dao.createRelationship(fromUserId, toUserId)){
                onLineUsers[fromUserId]?.send(Frame.Text("{\"command\": \"server-add-friend\" , \"message\" : \"$email\", \"extraMessage\" : \"$toUserId\"}"));
                onLineUsers[toUserId]?.send(Frame.Text("{\"command\": \"server-friend-request\" , \"message\" : \"$userEmail\", \"extraMessage\" : \"$fromUserId\"}"));
            }
        }
        else
            onLineUsers[fromUserId]?.send(Frame.Text("{\"command\": \"server-add-friend\" , \"message\" : \"failure\"}"));
    }

    private suspend fun acceptFriend(fromUserId: String, email : String?, dao: DAOFacade) {
        if (email != null){
            val toUserId = dao.userByEmail(email)?.userId
            val userEmail = dao.user(fromUserId)?.email

            if (toUserId != null && fromUserId != toUserId && dao.confirmRelationship(fromUserId, toUserId)){
                onLineUsers[fromUserId]?.send(Frame.Text("{\"command\": \"server-accept-friend\" , \"message\" : \"$email\", \"extraMessage\" : \"$toUserId\"}"));
                onLineUsers[toUserId]?.send(Frame.Text("{\"command\": \"server-accept-request\" , \"message\" : \"$userEmail\", \"extraMessage\" : \"$fromUserId\"}"));
            }
        }
        else
            onLineUsers[fromUserId]?.send(Frame.Text("{\"command\": \"server-accept-friend\" , \"message\" : \"failure\"}"));
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