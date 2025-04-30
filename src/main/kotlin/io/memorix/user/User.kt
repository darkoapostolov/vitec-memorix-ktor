package io.memorix.user

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object Users : Table() {
    private val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class UserRequest(val name: String, val email: String, val password: String)

@Serializable
data class UserResponse(val email: String, val name: String)

@Serializable
data class UsersResponse(val users: List<UserResponse>, val total: Long)

@Serializable
data class ErrorResponse(val error: String)