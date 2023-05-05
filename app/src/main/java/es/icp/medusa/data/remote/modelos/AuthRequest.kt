package es.icp.medusa.data.remote.modelos

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AuthRequest(
    @SerializedName("u")
    val username: String = "",
    @SerializedName("p")
    val password : String = ""
) : Serializable
