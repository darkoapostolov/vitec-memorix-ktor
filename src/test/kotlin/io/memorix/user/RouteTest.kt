package io.memorix.user

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.serialization.json.Json
import org.junit.Test

class RouteTest {
    private val mockRepo = mockk<UserRepository>()

    private fun Application.testModule() {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        routing {
            userRoutes(mockRepo)
        }
    }

    @Test
    fun `POST users - duplicate email`() = testApplication {
        application {
            testModule()
        }

        val request = UserRequest("existing@email.com", "Test", "Test")

        coEvery { mockRepo.isEmailTaken(request.email) } returns true

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(UserRequest.serializer(), request))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val body = bodyAsText()
            assertTrue(body.contains("Duplicate e-mail"))
        }
    }

    @Test
    fun `POST users - success`() = testApplication {
        application {
            testModule()
        }

        val request = UserRequest("new@email.com", "New User", "Test")

        coEvery { mockRepo.isEmailTaken(request.email) } returns false
        coJustRun { mockRepo.createUser(request) }


        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(UserRequest.serializer(), request))
        }.apply {
            assertEquals(HttpStatusCode.Accepted, status)
        }
    }

    @Test
    fun `GET users - success with query`() = testApplication {
        application {
            testModule()
        }

        val users = listOf(UserResponse("new@email.com", "New User"))
        coEvery { mockRepo.getUsers("new", 5) } returns users
        coEvery { mockRepo.getTotalCount() } returns 1

        client.get("/users?query=new&limit=5").apply {
            assertEquals(HttpStatusCode.OK, status)

            val response = Json.decodeFromString(
                UsersResponse.serializer(),
                bodyAsText()
            )

            assertEquals(1, response.total)
            assertEquals("new@email.com", response.users.first().email)
        }
    }
}