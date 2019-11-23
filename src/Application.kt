package com.duyvu


import com.duyvu.dao.DAOFacade
import com.duyvu.dao.DAOFacadeDatabase
import com.duyvu.model.SocketServer
import com.mchange.v2.c3p0.ComboPooledDataSource
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
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
import io.ktor.util.hex
import io.ktor.websocket.WebSockets
import io.ktor.http.cio.websocket.*
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.sessions.*
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach
import org.jetbrains.exposed.sql.Database
import java.io.File
import java.sql.Driver
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.time.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

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
fun Application.module() {
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

    install(WebSockets) {
        pingPeriod = Duration.ofMinutes(1)
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

        webSocket("/ws") {
            val session = call.sessions.get<ApplicationSession>()

            if (session == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                return@webSocket
            }

            server.userOnline(session.userId, this)

            try {
                incoming.consumeEach { frame ->

                    if (frame is Frame.Text) {

                        val jsonData = json.parse(SocketData.serializer(), frame.readText())
                        server.command(session.userId, jsonData, dao)
                    }
                }
            } finally {
                server.userOffline(session.userId, this)
            }

        }

        static("/styles") {
            resources("styles")
        }
    }
}

val hashKey = hex("6819b57a326945c1968f45236589")

val dir = File("build/db")

val hmacKey = SecretKeySpec(hashKey, "HmacSHA1")

val json = Json(JsonConfiguration.Stable)

@Serializable
data class SocketData(val command:String?, val message: String?, val extraMessage: String?)

val server = SocketServer()

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
    val host = request.host()
    val portSpec = request.port().let { if (it == 80) "" else ":$it" }
    val address = host + portSpec

    respondRedirect("http://$address${application.locations.href(location)}")
}