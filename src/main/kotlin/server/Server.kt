package server

import javafx.application.Application.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLServerSocketFactory
import kotlin.system.exitProcess

fun main() = runBlocking  {
    val puerto = 6969
    var numclientes = 0
    var salir = true

    val fichero = System.getProperty("user.dir") + File.separator + "cert" + File.separator + "sender_keystore.p12"
    if (!Files.exists(Path.of(fichero))) {
        System.err.println("No se encuentra el fichero de certificado del servidor")
        exitProcess(0)
    }


    //  System.setProperty("javax.net.debug", "ssl, keymanager, handshake") // Depuramos
    System.setProperty("javax.net.ssl.keyStore", fichero) // Llavero
    System.setProperty("javax.net.ssl.keyStorePassword", "password") // Clave de acceso

    val serverFactory = SSLServerSocketFactory.getDefault() as SSLServerSocketFactory
    val serverSocket = serverFactory.createServerSocket(puerto) as SSLServerSocket




    println("Servidor arrancado y esperando conexiones...")
    try {


        while (salir) {
            println("Esperando...")
            val socket = serverSocket.accept()
            // cliente = servidor.accept()
            numclientes++
            println("Cliente $numclientes conectado${socket.inetAddress} --- ${socket.port}")
            val gc = GestorClientes(socket,"TecoSecret",10000)

            launch(Dispatchers.IO) {
                gc.procesarCliente()
            }




        }
//        println("Servidor finalizado...")
//        servidor.close()
//
    } catch (e: Exception) {
        e.printStackTrace()
    }


}