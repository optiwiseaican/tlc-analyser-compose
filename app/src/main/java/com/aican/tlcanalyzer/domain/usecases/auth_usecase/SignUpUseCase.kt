package com.aican.tlcanalyzer.domain.usecases.auth_usecase

import com.aican.tlcanalyzer.data.repository.auth.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val authRepository: AuthRepository) {

    suspend operator fun invoke(name: String, email: String, password: String): Result<Boolean> {

        return authRepository.signUp(name, email, password)

    }
}