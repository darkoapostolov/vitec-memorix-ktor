package io.memorix.user

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class UserRepository(private val database: Database) {

    fun isEmailTaken(email: String): Boolean = transaction(database) {
        Users.select { Users.email eq email }.count() > 0
    }

    fun createUser(request: UserRequest): Unit = transaction(database) {
        Users.insert {
            it[name] = request.name
            it[email] = request.email
            it[password] = BCrypt.hashpw(request.password, BCrypt.gensalt())
        }
    }

    fun getUsers(query: String, limit: Int): List<UserResponse> = transaction(database) {
        Users
            .select { Users.name.lowerCase() like "$query%" }
            .limit(limit)
            .map { UserResponse(it[Users.email], it[Users.name]) }
    }

    fun getTotalCount(): Long = transaction(database) {
        Users.selectAll().count()
    }
}
