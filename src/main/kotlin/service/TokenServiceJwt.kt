package service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import models.Usuario
import java.util.*


object TokenService {
    fun createToken(user: Usuario, tokeSecret: String, tokenExpiration: Long): String {
        println("Creando token")
        val algorithm: Algorithm = Algorithm.HMAC256(tokeSecret)
        // Ahora creamos el token, no todos los campos son obligatorios. Te comento algunos con *
        return JWT.create()
            .withIssuer("TECOCLASS") // Quien lo emite *
            //.withSubject("Programacion de Servicios y Procesos") // Para que lo emite *
            .withClaim("usuarioid", user.id) // Datos que queremos guardar * (al menos algunos)
            .withClaim("username", user.username) // Datos que queremos guardar
            .withClaim("role", user.role) // Datos que queremos guardar
            .withIssuedAt(Date()) // Fecha de emision *
            .withExpiresAt(Date(System.currentTimeMillis() + tokenExpiration)) // Fecha de expiracion *
            .sign(algorithm) // Firmamos el token
    }

    fun verifyToken(token: String, tokeSecret: String, user: Usuario): Boolean {
        println("Verificando token")
        val algorithm: Algorithm = Algorithm.HMAC256(tokeSecret)
        val verifier = JWT.require(algorithm)
            .build() // Creamos el verificador
        return try {
            val decodedJWT = verifier.verify(token)
           println("Token verificado" )
            // Comprobamos que el token es del usuario
                   //TODO PREGUNTAR SI ESTO SERIA ASI
            decodedJWT.getClaim("usuarioid").asInt() == user.id &&
                    decodedJWT.getClaim("username").asString() == user.username &&
                    decodedJWT.getClaim("role").asString() == user.role


        } catch (e: Exception) {
            println("Error al verificar el token: ${e.message}" )
            false
        }
    }
}