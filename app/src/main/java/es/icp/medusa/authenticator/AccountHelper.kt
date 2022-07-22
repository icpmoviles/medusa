package es.icp.medusa.authenticator

import android.accounts.Account
import android.accounts.AccountManager
import android.text.TextUtils
import com.google.gson.Gson
import es.icp.medusa.modelo.TokenResponse
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
    cuentas?.let { it.forEach { account ->
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


fun AccountManager.isAccessGranted(account: Account): Boolean{
    val token = this.peekAuthToken(account, MY_AUTH_TOKEN_TYPE)
    if (!token.isNullOrEmpty()) {
        try {
            val tokenResponse = getTokenResponse(account)
            if (isTokenInTime(tokenResponse.dateExpire))
                return true
        } catch (ex: Exception){
            return false
        }

    }
    return false
}

fun AccountManager.clearToken(account: Account) =
    this.invalidateAuthToken(MY_ACCOUNT_TYPE, this.peekAuthToken(account, MY_AUTH_TOKEN_TYPE))

