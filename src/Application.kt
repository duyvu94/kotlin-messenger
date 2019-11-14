package com.duyvu


import com.duyvu.model.DAOFacade
import com.duyvu.model.DAOFacadeDatabase
import com.mchange.v2.c3p0.ComboPooledDataSource
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import freemarker.cache.*
import io.ktor.freemarker.*
import io.ktor.features.CallLogging
import io.ktor.features.ConditionalHeaders
import io.ktor.features.DefaultHeaders
import io.ktor.features.PartialContent
import io.ktor.http.content.*
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.locations.locations
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.util.hex
import org.jetbrains.exposed.sql.Database
import java.io.File
import java.net.URI
import java.sql.Driver
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Location("/")
class Welcome()

@Location("/login")
data class Login(val userId: String = "", val error: String = "")

@Location("/logout")
class Logout()

@Location("/register")
data class Register(val userId: String = "", val displayName: String = "", val email: String = "", val error: String = "")

@Location("/messenger")
data class Messenger(val userId: String = "")

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    dao.init()
    environment.monitor.subscribe(ApplicationStopped) { pool.close() }

    //install application's components
    install(DefaultHeaders)
    install(CallLogging)
    install(ConditionalHeaders)
    install(PartialContent)
    install(Locations)

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(Sessions) {
        cookie<ApplicationSession>("SESSION") {
            transform(SessionTransportTransformerMessageAuthentication(hashKey))
        }
    }

    val hashFunction = { s: String -> hash(s) }

    //register routes
    routing {

        welcome()
        login(dao, hashFunction)
        register(dao, hashFunction)
        messenger(dao)

        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/styles") {
            resources("styles")
        }
    }
}

val hashKey = hex("6819b57a326945c1968f45236589")

val dir = File("build/db")

val hmacKey = SecretKeySpec(hashKey, "HmacSHA1")

val pool = ComboPooledDataSource().apply {
    driverClass = Driver::class.java.name
    jdbcUrl = "jdbc:h2:file:${dir.canonicalFile.absolutePath}"
    user = ""
    password = ""
}

fun hash(password: String): String {
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}

val dao: DAOFacade = DAOFacadeDatabase(Database.connect(pool))

data class ApplicationSession(val userId: String)

internal fun userNameValid(userId: String) = userId.matches("[a-zA-Z0-9_\\.]+".toRegex())

suspend fun ApplicationCall.redirect(location: Any) {
    val host = request.host() ?: "localhost"
    val portSpec = request.port().let { if (it == 80) "" else ":$it" }
    val address = host + portSpec

    respondRedirect("http://$address${application.locations.href(location)}")
}