package es.icp.medusa.modelo

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class TokenResponse(

    @SerializedName("access_token")
    var accessToken: String = "",

    @SerializedName("expires_in")
    var expiresIn : Int = 0,

    @SerializedName("message")
    var message: String = "",

    @SerializedName("refresh_token")
    var refreshToken : String = "",

    @SerializedName("scope")
    var scope: String = "",

    @SerializedName("status")
    var status: String = "",

    @SerializedName("token_type")
    val tokenType: String = "Bearer",

    var dateExpire: Date = Date()
) : Serializable