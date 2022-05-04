package es.icp.medusa.modelo

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class TokenResponse(

    @SerializedName("accessToken")
    var accessToken: String = "",

    @SerializedName("expiresIn")
    var expiresIn : Int = 0,

    @SerializedName("message")
    var message: String = "",

    @SerializedName("refreshToken")
    var refreshToken : String = "",

    @SerializedName("scope")
    var scope: String = "",

    @SerializedName("status")
    var status: String = "",

    @SerializedName("tokenType")
    val tokenType: String = "Bearer",

    var dateExpire: Date = Date()
) : Serializable