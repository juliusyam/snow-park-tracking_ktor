package com.juliusyam.services

import com.google.gson.Gson
import com.juliusyam.models.User
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

class UserService(database: MongoDatabase) {
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
                call.respond(HttpStatusCode.Accepted, Gson().toJson(it))
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
