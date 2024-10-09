package com.juliusyam.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginPayload(
    val username: String,
    val password: String,
)
