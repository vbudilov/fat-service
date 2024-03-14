package com.budilov.fat

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val error: Boolean? = false,
    val errorMessage: String? = null,
    val body: T? = null
)
