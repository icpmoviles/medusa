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
import es.icp.medusa.repo.interfaces.RepoResponse
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

fun AccountManager.isValidToken(account: Account): Boolean{
    val token : String? = this.getToken(account)
    Log.w("acoount", token.toString())
    token?.let {
        return true
    }

//    if (!token.isNullOrEmpty()) {
//        try {
//            val tokenResponse = getTokenResponse(account)
//            if (isTokenInTime(tokenResponse.dateExpire))
//                return true
//        } catch (ex: Exception){
//            return false
//        }
//
//    }
    return false
}

fun AccountManager.getToken(account: Account) : String? =
    this.peekAuthToken(account, MY_AUTH_TOKEN_TYPE)


fun AccountManager.clearToken(account: Account) =
    this.invalidateAuthToken(MY_ACCOUNT_TYPE, this.peekAuthToken(account, MY_AUTH_TOKEN_TYPE))

fun isTokenValid(context: Context, token: String, resultado: (Boolean) -> Unit) {

    WebServiceLogin.isTokenValid(
        context,
        token
    ) {
        Log.w("istokenvalid", it.toString())
        resultado.invoke(it)
    }

}