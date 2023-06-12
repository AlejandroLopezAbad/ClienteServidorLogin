package repositories

import models.Usuario
import org.mindrot.jbcrypt.BCrypt

object UsuarioRepository {
    val usuarios = listOf(
        Usuario(
            id = 1,
            nombre = "Alexitto",
            username = "byalexitto",
            password = BCrypt.hashpw("Hola", BCrypt.gensalt(12)),
            role = "user"
        ),
        Usuario(
            id = 2,
            nombre = "Teco Teco",
            username = "Teco",
            password = BCrypt.hashpw("teco1234", BCrypt.gensalt(12)),
            role = "admin"
        ),
    )

    fun findUserByUsername(username: String): Usuario? {
        return usuarios.find { it.username == username }
    }

    fun findUserById(id: Int): Usuario? {
        return usuarios.find { it.id == id }
    }

}
