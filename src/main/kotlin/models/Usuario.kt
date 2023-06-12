package models

import kotlinx.serialization.Serializable

data class Usuario(
    val id :Int,
    val nombre:String,
    val username:String,
    val password:String,
    val role : String,

) {
}




@Serializable
data class Login(
    val username: String,
    val password: String
)