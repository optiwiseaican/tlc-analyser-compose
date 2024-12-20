package com.aican.tlcanalyzer.domain.model.auth

sealed class AuthState {
    data object Initial : AuthState()
    data object Loading: AuthState()

    data class Success(val message: String): AuthState()
    data class Error(val message: String): AuthState()


}