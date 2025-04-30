package io.memorix.user

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mindrot.jbcrypt.BCrypt

class UserRepositoryTest {

    private lateinit var db: Database
    private lateinit var repo: UserRepository

    @Before
    fun setUp() {
        db = Database.connect(
            "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver"
        )
        transaction(db) {
            create(Users)
        }
        repo = UserRepository(db)
    }

    @After
    fun tearDown() {
        transaction(db) {
            drop(Users)
        }
    }

    @Test
    fun `isEmailTaken returns false for new email`() {
        val result = repo.isEmailTaken("not@taken.com")
        assertFalse(result)
    }

    @Test
    fun `isEmailTaken returns true for existing email`() {
        repo.createUser(UserRequest("Test User", "test@domain.com", "password"))
        val result = repo.isEmailTaken("test@domain.com")
        assertEquals(true, result)
    }

    @Test
    fun `createUser inserts user and hashes password`() {
        val request = UserRequest("Hash User", "hash@domain.com", "secret")
        repo.createUser(request)
        transaction(db) {
            val row = Users.select { Users.email eq "hash@domain.com" }.singleOrNull()
            assertNotNull(row)
            assertEquals("Hash User", row!![Users.name])
            assertNotEquals("secret", row[Users.password])
            assertTrue(BCrypt.checkpw("secret", row[Users.password]))
        }
    }

    @Test
    fun `getUsers returns filtered and limited users`() {
        repo.createUser(UserRequest("Alice", "a@email.com", "pw"))
        repo.createUser(UserRequest("Bob", "b@email.com", "pw"))
        repo.createUser(UserRequest("Carol", "c@email.com", "pw"))
        val users = repo.getUsers("a", 10)
        assertEquals(1, users.size)
        assertEquals("Alice", users[0].name)
        val allUsers = repo.getUsers("", 2)
        assertEquals(2, allUsers.size)
    }

    @Test
    fun `getTotalCount returns correct count`() {
        assertEquals(0, repo.getTotalCount())
        repo.createUser(UserRequest("One", "1@email.com", "pw"))
        repo.createUser(UserRequest("Two", "2@email.com", "pw"))
        assertEquals(2, repo.getTotalCount())
    }
}
