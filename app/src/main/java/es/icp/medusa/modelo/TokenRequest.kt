package es.icp.medusa.modelo

import com.google.gson.annotations.SerializedName
import es.icp.medusa.modelo.comunes.BaseObject

data class TokenRequest(
    @SerializedName("u")
    val username: String = "",
    @SerializedName("p")
    val password : String = ""
) : BaseObject()
