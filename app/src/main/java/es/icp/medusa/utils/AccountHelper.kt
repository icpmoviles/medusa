package es.icp.medusa.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import es.icp.medusa.data.remote.modelos.AuthRequest
import es.icp.medusa.data.remote.modelos.AuthResponse
import es.icp.medusa.utils.ConstantesAuthPerseo.KEY_ACTIVE_ACCOUNT
import es.icp.medusa.utils.ConstantesAuthPerseo.KEY_AUHT_RESPONSE
import es.icp.medusa.utils.ConstantesAuthPerseo.MY_ACCOUNT_TYPE
import es.icp.medusa.utils.ConstantesAuthPerseo.MY_AUTH_TOKEN_TYPE
import java.util.*

/**
 * Obtiene un listado de las cuentas de la aplicación en el dispositivo
 * @return Devuelve un array con las cuentas de la aplicación
 */
fun AccountManager.getMyAccounts(): Array<out Account> = this.getAccountsByType(MY_ACCOUNT_TYPE)

/**
 * Obtiene la cuenta a partir del nombre si esta existe
 * @param nameAccount Nombre de la cuenta
 * @return Devuelve la cuenta si existe, null en caso contrario
 */
fun AccountManager.getAccountByName(nameAccount: String): Account? {
    val cuentas = this.getAccountsByType(MY_ACCOUNT_TYPE)
    cuentas.forEach { cuenta ->
        if (cuenta.name.equals(nameAccount, false))
            return cuenta
    }
    return null
}

/**
 * Comprueba si existe una cuenta con el nombre especificado
 * @param nameAccount Nombre de la cuenta
 * @return Devuelve true si existe una cuenta con el nombre especificado, false en caso contrario
 */
fun AccountManager.existsAccountByName(nameAccount: String): Boolean {
    val cuentas = this.getAccountsByType(MY_ACCOUNT_TYPE)
    return cuentas.any { cuenta ->
        cuenta.name.equals(nameAccount, false)
    }
}

/**
 * Permite establecer un token de autenticación para una cuenta de usuario específica.
 * @param account Cuenta de usuario
 * @param token Token de autenticación
 */
fun AccountManager.setToken(account: Account, token: String) =
    this.setAuthToken(account, MY_AUTH_TOKEN_TYPE, token)

/**
 * Obtiene el token almacenado en la cuenta
 * @param account Cuenta de la que se quiere obtener el token
 * @return Token almacenado en la cuenta
 */
fun AccountManager.getToken(account: Account) : String? =
    this.peekAuthToken(account, MY_AUTH_TOKEN_TYPE)
/**
 * Obtiene el token de refresco almacenado en la cuenta
 * @param account Cuenta de la que se quiere obtener el token de refresco
 * @return Token de refresco almacenado en la cuenta
 */
fun AccountManager.getRefreshToken(account: Account) : String {
    val userDataString = this.getUserData(account, KEY_AUHT_RESPONSE)
    val userData =  Gson().fromJson(userDataString, AuthResponse::class.java)
    return userData.refreshToken
}
/**
 * Invalida el token almacenado en la cuenta
 * @param account Cuenta de la que se quiere invalidar el token
 */
fun AccountManager.clearToken(account: Account) =
    this.invalidateAuthToken(MY_ACCOUNT_TYPE, this.peekAuthToken(account, MY_AUTH_TOKEN_TYPE))


/**
 * Obtiene el nombre de usuario y la contraseña de la cuenta especificada
 * @param account Cuenta de la que se quiere obtener el nombre de usuario y la contraseña
 * @return Pair con el nombre de usuario y la contraseña
 *
 */
fun AccountManager.getUsernameAndPass(account: Account): Pair<String, String> {
    val username = account.name
    val password = this.getPassword(account)
    return Pair(username, password)
}

/**
 * Obtiene el nombre de usuario y la contraseña de la cuenta especificada
 * @param account Cuenta de la que se quiere obtener el nombre de usuario y la contraseña
 * @return Pair con el nombre de usuario y la contraseña
 *
 */
fun AccountManager.getAuthRequest(account: Account): AuthRequest {
    val username = account.name
    val password = this.getPassword(account)
    return AuthRequest(username, password)
}

/**
 * Crea una cuenta con el nombre de usuario y la contraseña especificados
 * @param username Nombre de usuario
 * @param password Contraseña
 * @param userData Datos adicionales que se quieren almacenar en la cuenta, por ejemplo, el token de autenticación o la información del usuario
 * @return true si la cuenta se ha creado correctamente, false en caso contrario
 */
fun AccountManager.createAccount(username: String, password: String, authResponse: AuthResponse, /*user:Any, appUserKey: String*/): Boolean {
    val account = Account(username, MY_ACCOUNT_TYPE)
//    authResponse.dateExpire = Date().addSeconds((authResponse.expiresIn - 100))
    val userData = Bundle().also { it.putString(KEY_AUHT_RESPONSE, Gson().toJson(authResponse))  }
    // creamos la cuenta
    val exito = this.addAccountExplicitly(account, password, userData)
    Log.w("CREAR CUENTA", "exito: $exito")
    // le metemos el token a la cuenta
    this.setAuthToken(account, MY_AUTH_TOKEN_TYPE, authResponse.accessToken)
    return exito
}

/**
 * Añade la información y el token de autenticación a la cuenta especificada
 * @param account Cuenta a la que se quiere añadir la información de autenticación
 * @param authResponse Información de autenticación (authResponse)
 */
fun AccountManager.setAuthResponse(authResponse: AuthResponse, account: Account) {
    this.setUserData(account, KEY_AUHT_RESPONSE, Gson().toJson(authResponse))
    this.setToken(account, authResponse.accessToken)
}

/**
 * Obtiene la información de autenticación de la cuenta especificada
 * @param account Cuenta de la que se quiere obtener la información de autenticación
 * @return Información de autenticación (authResponse)
 */
fun AccountManager.getAuthResponse(account: Account) : AuthResponse? {
    val userDataString = this.getUserData(account, KEY_AUHT_RESPONSE)
    return Gson().fromJson(userDataString, AuthResponse::class.java)
}




/**
 * Muestra un dialogo de seleccion de cuenta
 * @param context Contexto de la aplicacion
 * @param respuesta Funcion que se ejecuta cuando se selecciona una cuenta
 * @return Devuelve la cuenta seleccionada o null si no se selecciona ninguna
 */
fun AccountManager.choiceAccountDialog(context: Context, respuesta: (Account?) -> Unit){
    val accounts = this.getMyAccounts()
    val lista = ArrayList<String>()
    var eleccion: Int? = null

    accounts.forEach {
        lista.add(it.name)
    }
    val items = lista.toTypedArray()

    AlertDialog.Builder(context)
        .setTitle("Elige un usuario")
        .setCancelable(true)
        .setSingleChoiceItems(items, -1) { _, i ->
            eleccion = i
        }
        .setPositiveButton("ACEPTAR"){ _, _ ->

            val account = eleccion?.let { this.getAccountByName(items[it]) }
            respuesta.invoke(account)
        }
        .create()
        .show()
}

fun AccountManager.setActiveAccountByPackageName(account: Account, packageName: String){
    getMyAccounts().forEach {
        if (it.name == account.name)
            this.setUserData(it, packageName, "true")
        else
            this.setUserData(account, packageName, "true")
    }
}

fun AccountManager.getActiveAccountByPackageName(packageName: String): Account? =
    this.accounts.firstOrNull { this.getUserData(it, packageName) == "true" }

fun AccountManager.getTokenByAccountActive(context: Context): String? {
    val account = this.getActiveAccountByPackageName(context.applicationContext.packageName)
    return this.getToken(account ?: return null)
}

private fun Date.add(field: Int, amount: Int): Date {
    Calendar.getInstance().apply {
        time = this@add
        add(field, amount)
        return time
    }
}

private fun Date.addSeconds(seconds: Int): Date{
    return add(Calendar.SECOND, seconds)
}