package com.dsm.munaytripandroid.core.util

/**
 * Patrón Result para manejo de éxito/error
 * Estándar Google 2025 para operaciones asíncronas
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}