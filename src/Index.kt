package com.duyvu

import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

/**
 * Register the index route of the website.
 */
fun Route.index() {
    // Uses the location feature to register a get route for '/index'.
    get {
        val user = call.sessions.get<ApplicationSession>()?.let { dao.user(it.userId) }
        call.respond(FreeMarkerContent("index.ftl", mapOf("user" to user)))
    }
}
