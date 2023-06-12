package common

import kotlinx.serialization.Serializable

@Serializable
data class Request<T>(
    val contenido: T? = null, // Contenido de la petición
    val token: String? = null, // Token de autenticación
    val type: Type, // Tipo de petición
) {
    enum class Type {
        LOGIN, NUMERO
    }
}