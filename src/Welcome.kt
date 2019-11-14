package com.duyvu

import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

/**
 * Register the index route of the website.
 */
fun Route.welcome() {
    // Uses the location feature to register a get route for '/welcome'.
    get {
        val user = call.sessions.get<ApplicationSession>()?.let { dao.user(it.userId) }
        call.respond(FreeMarkerContent("welcome.ftl", mapOf("user" to user)))
    }
}
