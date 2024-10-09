package com.juliusyam.utilities

import org.mindrot.jbcrypt.BCrypt

object Password {
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
}
