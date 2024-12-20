package com.aican.tlcanalyzer.data.repository.auth

interface AuthRepository {
    suspend fun signUp(name: String, email: String, password: String): Result<Boolean>
    suspend fun signIn(email: String, password: String): Result<Boolean>

}