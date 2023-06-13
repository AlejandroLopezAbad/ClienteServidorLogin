package client

import common.Request
import common.Response
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Login
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.math.BigInteger
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Path
import java.security.cert.X509Certificate
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlin.system.exitProcess


private const val PUERTO = 6969
private val json = Json { ignoreUnknownKeys = true }


fun main() {
    val direccion: InetAddress
    println("Iniciando Cliente")


    val fichero = System.getProperty("user.dir") + File.separator + "cert" + File.separator + "receiver_keystore.p12"
    if (!Files.exists(Path.of(fichero))) {
        System.err.println("No se encuentra el fichero de certificado del servidor")
        exitProcess(0)
    }

    // Mejor cargarmos el fichero de propiedades
    println("Cargando fichero de propiedades")
    //  System.setProperty("javax.net.debug", "ssl, keymanager, handshake") // Debug
    System.setProperty("javax.net.ssl.trustStore", fichero) // llavero cliente
    System.setProperty("javax.net.ssl.trustStorePassword", "password") // clave

    try {
        direccion = InetAddress.getLocalHost()
        val clientFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val socket = clientFactory.createSocket(direccion, PUERTO) as SSLSocket

        println("Conectado a Servidor ...")

     //   infoSession(socket)

        val entrada = DataInputStream(socket.inputStream)
        val salida = DataOutputStream(socket.outputStream)
        //Me logeo
        sendLoginRequest(salida)
        //recibo el token si me he logeado bien
        val token = readTokenResponse(entrada)
        //Envio la peticion de multiplicar el numero

        sendNumRequest(salida,token)
        //CUANDO RECIBO ALGO DEL SERVIDOR EL METODO TIENE QUE TENER UN WHEN PARA SABER DE QUE TIPO ME ESTA ENVIADO EL RESPONSE
        val num = readNumResponse(entrada)



    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
    }
}

private fun readNumResponse(entrada: DataInputStream):String{
    val jsonNumResponse= entrada.readUTF()

    val response = json.decodeFromString<Response<String>>(jsonNumResponse)

    when(response.type){
        Response.Type.NUMERO->{
            println("Recibido el numero multiplicado del servidor : ${response.content}")
            return response.content!!
        }
        Response.Type.ERROR -> {
            println(" Error: ${response.content}")
            exitProcess(1)
        }
        else -> {
            println("❌ Error: Tipo de respuesta no esperado")
            exitProcess(1)}
    }

}

private fun sendNumRequest(salida:DataOutputStream,token:String){
    println("Que numero vas a querer enviar ")
    val num = readln()
    val requestNum= Request<String>(contenido = num.toString(),token=token, type = Request.Type.NUMERO)
    val jsonRequestNum= json.encodeToString(requestNum)
    println("Enviado : $jsonRequestNum")
    salida.writeUTF(jsonRequestNum)


}



private fun readTokenResponse(entrada:DataInputStream):String{

    val jsonReponse = entrada.readUTF()

    val response = json.decodeFromString<Response<String>>(jsonReponse)

    when(response.type){
        Response.Type.TOKEN->{
            println("Token Recibido del Servidor ${response.content}")
            return response.content!!

        }

        Response.Type.ERROR->{
            println("Error : ${response.content} ")
            exitProcess(1)
        }

        else -> {
            println(" Error: Tipo de respuesta no esperado")
            exitProcess(1)
        }
    }

}


private fun sendLoginRequest(salida: DataOutputStream) {

    val requestLogin = Request(contenido = Login("Teco", "teco1234"), type = Request.Type.LOGIN)

    val jsonRequest = json.encodeToString(requestLogin)

    println("Enviando Request de Login")
    salida.writeUTF(jsonRequest)

}

private fun infoSession(socket: SSLSocket) {
    println("🔐 Información de la sesión")
    try {
        val sesion: SSLSession = socket.session
        println("Servidor: " + sesion.peerHost)
        println("Cifrado: " + sesion.cipherSuite)
        println("Protocolo: " + sesion.protocol)
        println("Identificador:" + BigInteger(sesion.id))
        println("Creación de la sesión: " + sesion.creationTime)
        val certificado: X509Certificate = sesion.peerCertificates[0] as X509Certificate
        println("Propietario : " + certificado.subjectX500Principal)
        println("Algoritmo: " + certificado.sigAlgName)
        println("Tipo: " + certificado.type)
        println("Número Serie: " + certificado.serialNumber)
        // expiración del certificado
        println("Válido hasta: " + certificado.notAfter)
    } catch (ex: SSLPeerUnverifiedException) {
        System.err.println("Error en la sesión: ${ex.localizedMessage}")
    }
}