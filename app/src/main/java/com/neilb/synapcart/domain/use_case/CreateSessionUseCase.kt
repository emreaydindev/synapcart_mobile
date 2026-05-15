package com.neilb.synapcart.domain.use_case

import com.neilb.synapcart.data.model.SessionResponse
import com.neilb.synapcart.domain.repository.SynapCartRepository

class CreateSessionUseCase(private val repository: SynapCartRepository) {
    suspend operator fun invoke(): Result<SessionResponse> {
        return repository.createSession()
    }
}