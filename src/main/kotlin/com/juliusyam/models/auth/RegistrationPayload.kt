package com.juliusyam.models.auth

import kotlinx.serialization.Serializable

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
)
