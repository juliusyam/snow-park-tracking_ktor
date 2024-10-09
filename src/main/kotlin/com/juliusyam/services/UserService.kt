package com.juliusyam.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import com.juliusyam.models.User
import com.juliusyam.models.UserResponse
import com.juliusyam.models.auth.LoginPayload
import com.juliusyam.models.auth.RegistrationPayload
import com.juliusyam.utilities.Password
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class UserService(database: MongoDatabase, private val environment: ApplicationEnvironment) {
    private val collection: MongoCollection<Document>

    init {
        database.createCollection("users")
        collection = database.getCollection("users")
    }

    suspend fun registerUser(call: ApplicationCall, payload: RegistrationPayload) = withContext(Dispatchers.IO) {

        //TODO: Check Password Strength and Validity

        if (usernameIsOccupied(payload.username)) {
            call.respond(HttpStatusCode.Forbidden, "Username is already taken")
        }

        if (emailIsOccupied(payload.email)) {
            call.respond(HttpStatusCode.Forbidden, "Email is already taken")
        }

        val passwordHash = Password.hashPassword(payload.password)

        val user = User.createUser(payload, passwordHash)

        val doc = user.toDocument()
        collection.insertOne(doc)
        val id = doc["_id"].toString()

//        val userWithId = user.provideId(id)

        println("User ID: $id")

        call.respond(HttpStatusCode.Created, Gson().toJson(user))
    }

    suspend fun login(call: ApplicationCall, payload: LoginPayload) = withContext(Dispatchers.IO) {

        val user = collection.find(Filters.eq("username", payload.username)).first()
            ?.let(User.Companion::fromDocument)

        user?.let {
            if (BCrypt.checkpw(payload.password, it.passwordHash)) {
                val secret = environment.config.property("jwt.secret").getString()
                val issuer = environment.config.property("jwt.issuer").getString()
                val audience = environment.config.property("jwt.audience").getString()

                // TODO: Retrieve User Id
                val token = JWT.create()
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .withClaim("username", user.username)
                    .withClaim("firstName", user.firstName)
                    .withAudience("lastName", user.lastName)
                    .withExpiresAt(Date(System.currentTimeMillis() + 60_000))
                    .sign(Algorithm.HMAC256(secret))

                call.respond(Gson().toJson(UserResponse(it, token)))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Password is incorrect")
            }
        } ?: run {
            call.respond(HttpStatusCode.NotFound, "User not found")
        }
    }

    private fun usernameIsOccupied(username: String): Boolean =
        collection.find(Filters.eq("username", username)).first() != null

    private fun emailIsOccupied(email: String): Boolean =
        collection.find(Filters.eq("email", email)).first() != null
}
