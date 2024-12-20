package com.aican.tlcanalyzer.viewmodel.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aican.tlcanalyzer.domain.model.auth.AuthState
import com.aican.tlcanalyzer.domain.usecases.auth_usecase.SignInUseCase
import com.aican.tlcanalyzer.domain.usecases.auth_usecase.SignUpUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val signInUseCase: SignInUseCase,
    val signUpUseCase: SignUpUseCase
) :
    ViewModel() {

    var authState by mutableStateOf<AuthState>(AuthState.Initial)
        private set

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authState = AuthState.Loading
            val result = signInUseCase(email, password)
            authState = if (result.isSuccess) {
                AuthState.Success("Sign In Successful")
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Sign In failed")

            }

        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            authState = AuthState.Loading
            val result = signUpUseCase(name, email, password)
            authState = if (result.isSuccess) {
                AuthState.Success("Sign Up Successful")
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }


}