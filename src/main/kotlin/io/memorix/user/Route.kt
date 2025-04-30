package io.memorix.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userRepository: UserRepository) {

    post("/users") {
        val request = call.receive<UserRequest>()

        if (userRepository.isEmailTaken(request.email)) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                ErrorResponse("Duplicate e-mail: ${request.email}")
            )
            return@post
        }

        userRepository.createUser(request)

        call.respond(HttpStatusCode.Accepted, "")
    }

    get("/users") {
        val query = call.request.queryParameters["query"]?.lowercase() ?: ""
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

        val users = userRepository.getUsers(query, limit)
        val total = userRepository.getTotalCount()

        call.respond(UsersResponse(users, total))
    }
}
