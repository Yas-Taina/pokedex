package com.example.pokedex.models

data class User(
    val id: Int,
    val name: String,
    val login: String
)

data class RegisterRequest(
    val name: String,
    val login: String,
    val password: String
)

data class LoginRequest(
    val login: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val user: User
)