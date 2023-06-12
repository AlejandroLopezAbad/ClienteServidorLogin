package server

import common.Request
import common.Response
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Login
import models.Usuario
import org.mindrot.jbcrypt.BCrypt
import repositories.UsuarioRepository
import service.TokenService
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.OutputStream
import java.net.Socket


private val json = Json { ignoreUnknownKeys = true }

class GestorClientes(val socket: Socket, tokensecret: String, tokenExpiration: Long) {
    val tokenSecret = tokensecret
    val tokenExpiration = tokenExpiration

    fun procesarCliente() {

        val entrada = DataInputStream(socket.inputStream)
        val salida = DataOutputStream(socket.outputStream)

        val usuario = recibirLoginRequest(entrada)

        if (usuario == null) {
            sendErrorResponse(salida, "Error:Usuario o contraseña incorrecta")
        } else {

            enviarTokenResponse(salida, usuario, tokenSecret, tokenExpiration)

            if (usuario.role != "admin") {
                sendErrorResponse(salida, "ERROR :No tienes permisos de admin")
            } else {
                val numOk = recieveNumRequest(entrada, usuario, tokenSecret)
                //DEVOLVEMOS UN BOOLEAN para comprobar si el
                // token esta bien y si esta bien mandamos la peticion del numero
                if (!numOk) {
                    sendErrorResponse(salida, "Error: Token no valido")

                } else {
                    sendNumResponse(entrada, salida)

                }

            }

        }


    }

    fun sendNumResponse(entrada: DataInputStream, salida: DataOutputStream) {
        val numRequest = entrada.readUTF()
        val requestNum = json.decodeFromString<Request<String>>(numRequest)

        val num = requestNum.contenido?.toInt()

        val salidaNum = num?.times(2)

        val numResponse = Response(salidaNum.toString(),Response.Type.NUMERO)
        val jsonResponseNum= json.encodeToString(numResponse)

        salida.writeUTF(jsonResponseNum)

    }


    fun recieveNumRequest(entrada: DataInputStream, usuario: Usuario, tokensecret: String): Boolean {
        val jsonRequest = entrada.readUTF()
        val requestNum = json.decodeFromString<Request<String>>(jsonRequest)

        val token = requestNum.token!!
        return TokenService.verifyToken(token, tokenSecret, usuario)

    }


    fun enviarTokenResponse(salida: DataOutputStream, usuario: Usuario, tokensecret: String, tokenExpiration: Long) {

        val token = TokenService.createToken(usuario, tokensecret, tokenExpiration)

        val tokenResponse = Response(token, Response.Type.TOKEN)
        val jsonToken = json.encodeToString(tokenResponse)

        salida.writeUTF(jsonToken)

    }


    fun recibirLoginRequest(entrada: DataInputStream): Usuario? {

        val jsonLogin = entrada.readUTF() //recibo un utf  porque en el cliente he
        // hecho un writeUTF, entonces lo leo y lo tengo que decodificar  REQUEST DE TIPO LOGIN

        val requestLogin = json.decodeFromString<Request<Login>>(jsonLogin)

        val login = requestLogin.contenido as Login

        val usuario = UsuarioRepository.findUserByUsername(login.username)

        return if (usuario != null && BCrypt.checkpw(login.password, usuario.password) /*&& user.role == "admin"*/) {
            println("Usuario válido")
            usuario
        } else {
            println("Usuario no válido")
            null
        }

    }

    fun sendErrorResponse(salida: DataOutputStream, message: String) {

        val errorResponse = Response(message, Response.Type.ERROR)
        val jsonResponse = json.encodeToString(errorResponse)
        println("Enviado $jsonResponse")
        salida.writeUTF(jsonResponse)

    }


}