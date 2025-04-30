package io.memorix

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.memorix.plugins.configureHTTP
import io.memorix.plugins.configureRouting
import io.memorix.plugins.configureSerialization
import io.memorix.user.Users
import io.memorix.user.userDi
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.environmentProperties

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val db = Database.connect(
        url = "jdbc:postgresql://db:5432/ktor?sslmode=disable",
        user = "root",
        password = "root"
    )

    transaction(db) {
        SchemaUtils.create(Users)
    }

    startKoin {
        properties(
            dotenv {
                ignoreIfMalformed = true
                ignoreIfMissing = true
            }.entries().associate { it.key to it.value }
        )
        environmentProperties()
        modules(
            module {
                single { db }
            },
            userDi
        )
    }

    configureHTTP()
    configureSerialization()
    configureRouting()
}

