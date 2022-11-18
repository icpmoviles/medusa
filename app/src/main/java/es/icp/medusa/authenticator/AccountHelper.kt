package es.icp.medusa.authenticator

import android.accounts.Account
import android.accounts.AccountManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import es.icp.medusa.AuthViewModel
import es.icp.medusa.data.remote.modelos.response.AuthResponse
import es.icp.medusa.data.remote.modelos.response.UsuarioResponse
import es.icp.medusa.utils.Constantes
import java.util.*

const val MY_ACCOUNT_TYPE = "es.icp"
const val MY_AUTH_TOKEN_TYPE = "Bearer"
const val KEY_USERDATA_TOKEN = "userDataToken"
const val KEY_USERDATA_INFO = "userDataInfo"
const val KEY_BUNDLE_ACCOUNT = "account"
const val KEY_NAME_ACCOUNT = "nameAccount"
private val vm = AuthViewModel()

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

/**
 * OBTIENE LA CUENTA A TRAVES DEL NOMBRE SI ESTA EXISTE
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
 * OBTIENE EL MODELO DE DATOS DEL TOKEN RESPONSE
 */
fun AccountManager.getAuthResponse(account: Account): AuthResponse {
    val userData = this.getUserData(account, KEY_USERDATA_TOKEN)
    return Gson().fromJson(userData, AuthResponse::class.java)
}

/**
 * OBTIENE EL MODELO DE DATOS DEL USUARIO
 */
fun AccountManager.getUsusarioResponse(account: Account): UsuarioResponse {
    val userData = this.getUserData(account, KEY_USERDATA_INFO)
    return Gson().fromJson(userData, UsuarioResponse::class.java)
}


/**
 * OBTIENE EL TOKEN ALMACENADO EN LA CUENTA
 */
fun AccountManager.getAuthToken(account: Account) : String? =
    this.peekAuthToken(account, MY_AUTH_TOKEN_TYPE)

/**
 * INAVLIDA EL TOKEN ALMACENADO EN LA CUENTA
 */
fun AccountManager.clearAuthToken(account: Account) =
    this.invalidateAuthToken(MY_ACCOUNT_TYPE, this.peekAuthToken(account, MY_AUTH_TOKEN_TYPE))

/**
 * OBTIENE EL TOKEN DE REFRESCO DE LA INFORMACION DE LA CUENTA
 */
fun AccountManager.getAuthRefreshToken(account: Account) : String {
    val userDataString = this.getUserData(account, KEY_USERDATA_TOKEN)
    val userData =  Gson().fromJson(userDataString, AuthResponse::class.java)
    return userData.refreshToken
}

/**
 * VALIDA EL TOKEN ALMACENADO EN LA CUENTA CONTRA EL SERVIDOR
 */
fun AccountManager.isTokenValid (account: Account, resultado: (Boolean) -> Unit) {

    this.getAuthToken(account)?.let { token->

        vm.isTokenValid(){ valido ->
            Log.w("ext get token", valido.toString())
            if (valido){
                resultado.invoke(valido)
            } else {
                this.invalidateAuthToken(MY_AUTH_TOKEN_TYPE, token)
                resultado.invoke(false)
            }

        }
    }?: kotlin.run { resultado.invoke(false) }
}

/**
 * REFRESCA LOS DATOS DE ACCESO DEL USUARIO EN LA CUENTA
 */
fun AccountManager.refreshAuthToken(context: Context, account: Account, resultado: (Boolean) -> Unit){
    val currentToken = this.getAuthToken(account)
    currentToken?.let {  token ->
        val refreshToken = this.getAuthRefreshToken(account)
        vm.refreshAuthToken(
            Base64.encodeToString(refreshToken.toByteArray(), Base64.DEFAULT)
        ) { authResponse ->
            authResponse?.let {
                Log.w("refresh acc", it.toString())
                it.dateExpire = Date().addSeconds((it.expiresIn - 100))
                this.setUserData(
                    account,
                    KEY_USERDATA_TOKEN,
                    Gson().toJson(it)
                )
                setAlarm(context, it.dateExpire.time, account.name)
                this.setAuthToken(account, MY_AUTH_TOKEN_TYPE, it.accessToken)
                resultado.invoke(true)
            }
        }

    }?: kotlin.run { resultado.invoke(false) }
}


/**
 * OBTIENE LA FECHA EN FORMATO DATE DE LA EXPIRACION DEL TOKEN
 */
fun AccountManager.getTimeExpire(account: Account) : Date =
    this.getAuthResponse(account).dateExpire

/**
 * Add field date to current date
 */
fun Date.add(field: Int, amount: Int): Date {
    Calendar.getInstance().apply {
        time = this@add
        add(field, amount)
        return time
    }
}

fun Date.addSeconds(seconds: Int): Date{
    return add(Calendar.SECOND, seconds)
}

fun TextInputEditText.texto(texto: String){
    this.text?.clear()
    this.text?.append(texto)
}

fun setAlarm(context: Context, time: Long, nameAccount: String) {
    Log.w("setalarm", "configurada nueva alarma")
    //obteniendo el administrador de alarmas
    val am = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

    //creando una nueva intención especificando el receptor de transmisión
    val i = Intent(context, AlarmReciever::class.java).putExtra(KEY_NAME_ACCOUNT, nameAccount )

    //creando una intención pendiente usando la intención
    //configurar la alarma que se activará cuando expire el token
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        val pi = PendingIntent.getBroadcast(context, Constantes.REQUEST_CODE_FIN_SESION, i, PendingIntent.FLAG_CANCEL_CURRENT) // estab en 0
        am.set(AlarmManager.RTC,time,pi)
    }
    else {
        val pi = PendingIntent.getBroadcast(context, Constantes.REQUEST_CODE_FIN_SESION, i, PendingIntent.FLAG_MUTABLE)
        am.set(AlarmManager.RTC,time,pi)
    }

    //configurar la alarma que se activará cuando expire el token
//        am.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pi)

//        Toast.makeText(this, "La alarma está configurada", Toast.LENGTH_SHORT).show()
}