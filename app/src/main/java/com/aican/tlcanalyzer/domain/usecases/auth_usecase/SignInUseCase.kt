package com.aican.tlcanalyzer.domain.usecases.auth_usecase

import com.aican.tlcanalyzer.data.repository.auth.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend  operator fun invoke(email: String, password: String): Result<Boolean> {
        return authRepository.signIn(email, password)

    }

}