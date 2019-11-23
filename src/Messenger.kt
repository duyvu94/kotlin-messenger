package com.duyvu

import com.duyvu.dao.DAOFacade
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*


fun Route.messenger(dao: DAOFacade) {

    get<Messenger> {
        val user = call.sessions.get<ApplicationSession>()?.let { dao.user(it.userId) } ?: return@get call.redirect(Welcome())

        val friendRequestSentList = dao.friendRequestSentList(user.userId)
        val friendRequestWaitingList = dao.friendRequestWaitingList(user.userId)

        call.respond(FreeMarkerContent("messenger.ftl",
            mapOf("user" to user, "friendRequestSentList" to friendRequestSentList, "friendRequestWaitingList" to friendRequestWaitingList )))
    }
}
