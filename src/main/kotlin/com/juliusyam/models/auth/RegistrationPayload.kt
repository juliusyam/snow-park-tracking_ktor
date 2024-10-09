package com.juliusyam.models.auth

import com.juliusyam.models.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document

@Serializable
data class RegistrationPayload(
    val username: String,
    val email: String,
    val password: String,
    val gender: String,
    val dateOfBirth: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val address: String? = null,
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): User = json.decodeFromString(document.toJson())
    }
}
