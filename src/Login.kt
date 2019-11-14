package com.duyvu

import com.duyvu.model.DAOFacade
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*


fun Route.login(dao: DAOFacade, hash: (String) -> String) {

    get<Login> {
        val user = call.sessions.get<ApplicationSession>()?.let { dao.user(it.userId) }

        if (user != null) {
            call.redirect(Welcome())
        } else {
            call.respond(FreeMarkerContent("login.ftl", mapOf("userId" to it.userId, "error" to it.error), ""))
        }

    }

    post<Login> {
        val post = call.receive<Parameters>()
        val userId = post["userId"] ?: return@post call.redirect(it)
        val password = post["password"] ?: return@post call.redirect(it)

        val error = Login(userId)

        val login = when {
            userId.length < 4 -> null
            password.length < 6 -> null
            !userNameValid(userId) -> null
            else -> dao.user(userId, hash(password))
        }

        if (login == null) {
            call.redirect(error.copy(error = "Invalid username or password"))
        } else {
            call.sessions.set(ApplicationSession(login.userId))
            call.redirect(Welcome())
        }
    }


    get<Logout> {
        call.sessions.clear<ApplicationSession>()
        call.redirect(Welcome())
    }
}
