package com.juliusyam.models

import com.juliusyam.models.auth.RegistrationPayload
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import java.time.LocalDateTime
import java.time.ZoneOffset

@Serializable
data class User(
    val username: String,
    val email: String,
    val passwordHash: String,
    val gender: String,
    val dateOfBirth: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val address: String? = null,
    val dateCreated: String,
    val dateEdited: String? = null
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

//    fun provideId(id: String): User {
//        this._id = id
//        return this
//    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): User = json.decodeFromString(document.toJson())

        fun createUser(payload: RegistrationPayload, passwordHash: String): User {
            return User(
                payload.username,
                payload.email,
                passwordHash,
                payload.gender,
                payload.dateOfBirth,
                payload.firstName,
                payload.lastName,
                payload.address,
                LocalDateTime.now(ZoneOffset.UTC).toString(),
            )
        }
    }
}

data class UserResponse(val user: User, val token: String)

