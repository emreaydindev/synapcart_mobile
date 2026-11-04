package com.neilb.synapcart.domain.use_case.chat

import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.domain.repository.SynapCartRepository

class RenameSessionUseCase(private val repository: SynapCartRepository) {
    suspend operator fun invoke(sessionId: Int, title: String) = repository.renameSession(sessionId, title)
}

class DeleteSessionUseCase(private val repository: SynapCartRepository) {
    suspend operator fun invoke(sessionId: Int) = repository.deleteSession(sessionId)
}

/** AI pros/cons and a verdict for 2–4 products (premium). */
class CompareProductsUseCase(private val repository: SynapCartRepository) {
    suspend operator fun invoke(products: List<ProductDTO>) = repository.compareProducts(products)
}

data class ChatUseCases(
    val getSessions: GetSessionsUseCase,
    val createSession: CreateSessionUseCase,
    val renameSession: RenameSessionUseCase,
    val deleteSession: DeleteSessionUseCase,
    val getMessages: GetMessagesUseCase,
    val compareProducts: CompareProductsUseCase
)
