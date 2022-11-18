package es.icp.medusa.data.remote.modelos.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LoginRequest(
    @SerializedName("u")
    val username: String = "",
    @SerializedName("p")
    val password : String = ""
) : Serializable
