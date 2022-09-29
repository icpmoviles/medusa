package es.icp.medusa.authenticator

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import es.icp.medusa.modelo.TokenResponse
import es.icp.medusa.modelo.UsuarioLogin
import es.icp.medusa.repo.WebServiceLogin
import java.util.*

const val MY_ACCOUNT_TYPE = "es.icp"
const val MY_AUTH_TOKEN_TYPE = "Bearer"
const val KEY_USERDATA_TOKEN = "userDataToken"
const val KEY_USERDATA_INFO = "userDataInfo"
const val KEY_BUNDLE_ACCOUNT = "account"
const val KEY_NAME_ACCOUNT = "nameAccount"

fun AccountManager.getMyAccounts(): Array<out Account>? = this.getAccountsByType(MY_ACCOUNT_TYPE)

fun isTokenInTime(dateExpire: Date): Boolean = Date().before(dateExpire)

fun AccountManager.existsAccountByName(nameAccount: String) : Boolean{
    val cuentas = this.getMyAccounts()
    cuentas?.let {
        it.forEach { account ->
            if (TextUtils.equals(account.name, nameAccount))
                return true
        }
    }
    return false
}

fun AccountManager.getPassword(account: Account?): String? {
    return try {
        this.getPassword(account)
    } catch (e: Exception) {
        null
    }
}

fun AccountManager.getAccountByName(nameAccount: String): Account? {
    val cuentas = this.getAccountsByType(MY_ACCOUNT_TYPE)
    cuentas.forEach { cuenta ->
        if (cuenta.name.equals(nameAccount, false))
            return cuenta
    }
    return null
}

fun AccountManager.getTokenResponse(account: Account): TokenResponse {
    val userData = this.getUserData(account, KEY_USERDATA_TOKEN)
    return Gson().fromJson(userData, TokenResponse::class.java)
}

fun AccountManager.getUserDataResponse(account: Account): UsuarioLogin {
    val userData = this.getUserData(account, KEY_USERDATA_INFO)
    return Gson().fromJson(userData, UsuarioLogin::class.java)
}

fun AccountManager.isAccountTokenValid(account: Account): Boolean{
    val token : String? = this.getToken(account)
    Log.w("acoount", token.toString())
    token?.let {
        return true
    } ?: kotlin.run { return false }

}

fun AccountManager.getToken(account: Account) : String? =
    this.peekAuthToken(account, MY_AUTH_TOKEN_TYPE)

fun AccountManager.clearToken(account: Account) =
    this.invalidateAuthToken(MY_ACCOUNT_TYPE, this.peekAuthToken(account, MY_AUTH_TOKEN_TYPE))


fun AccountManager.getRefreshToken(account: Account) : String {
    val userDataString = this.getUserData(account, KEY_USERDATA_TOKEN)
    val userData =  Gson().fromJson(userDataString, TokenResponse::class.java)
    return userData.refreshToken
}

fun AccountManager.isTokenValidFromServer(context: Context, account: Account, resultado: (Boolean) -> Unit) {

    this.getToken(account)?.let { token->
        WebServiceLogin.isTokenValid(
            context,
            token
        ) { resultado.invoke(it) }
    }?: kotlin.run {
        this.invalidateAuthToken(MY_AUTH_TOKEN_TYPE, this.getToken(account))
        resultado.invoke(false)
    }
}


fun AccountManager.refreshToken(context: Context, account: Account) : Boolean{
    val currentToken = this.getToken(account)
    currentToken?.let {
        WebServiceLogin.refreshToken(
            context,
            it,
            this.getRefreshToken(account)
        ){ response->
            this.setUserData(
                account,
                KEY_USERDATA_TOKEN,
                response
            )
            val newToken = Gson().fromJson(response, TokenResponse::class.java).accessToken
            this.setAuthToken(account, MY_AUTH_TOKEN_TYPE, newToken)
        }
        return true
    }?: kotlin.run { return false }
}

fun AccountManager.removeTokenAccount(account: Account) =
    this.invalidateAuthToken(MY_AUTH_TOKEN_TYPE, this.getToken(account))

fun AccountManager.getTimeExpire(account: Account) : Int =
    this.getTokenResponse(account).expiresIn
