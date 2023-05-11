package es.icp.medusa.repositorio

import android.accounts.Account
import android.accounts.AccountManager
import android.util.Log
import es.icp.genericretrofit.utils.*
import es.icp.medusa.data.remote.modelos.AuthRequest
import es.icp.medusa.data.remote.service.AuthService
import es.icp.medusa.utils.*


class AuthRepo constructor(
    private val authService: AuthService,
    private val am : AccountManager
    ) {

    /**
     * Obtiene el token de Perseo y lo guarda en el AccountManager ya sea creando una nueva cuenta o
     * actualizando la cuenta existente
     * @param request AuthRequest (username, password)
     * @return Pair<Boolean, String?>?
     *      true: si se ha obtenido el token
     *      false: si no se ha obtenido el token
     *      String: mensaje de error en caso de que no se haya obtenido el token
     */
    suspend fun getTokenPerseo( request: AuthRequest) : Pair<Boolean, String?>? {
        var exito : Pair<Boolean, String?>? = null
        authService.getTokenPerseo( request)
            .onSuccess { authResponse ->
                Log.w("authRepo", "getAuthToken: $authResponse")
                val cuenta = am.getAccountByName(request.username)
                cuenta?.let {
                    am.setAuthResponse(authResponse, it)
                }?: run {
                    am.createAccount(
                        username = request.username,
                        password = request.password,
                        authResponse = authResponse)
                }
                exito = Pair(true, null)
                return@onSuccess
            }
            .onError { code, message ->
                Log.w("onError getToken", "mensaje: $message code: $code")
                exito = Pair(false, message)

            }
            .onException { ex ->
                exito = Pair(false, ex.message)

            }
        return exito
    }

    /**
     * Comprueba si el token es válido contra el servidor
     * @param token Token a comprobar
     * @return Pair<Boolean, String?>?
     *     true: si el token es válido
     *     false: si el token no es válido
     *     String: mensaje de error en caso de que no se haya podido comprobar el token
     */
    suspend fun isTokenValid(account: Account) : Pair<Boolean, String?>? {
        var exito : Pair<Boolean, String?>? = null
        val token = am.getToken(account)
        token?.let {
            authService.isTokenValid("Bearer $it")
                .onSuccess { s ->
                    Log.w("isTokenValid", "onSuccess: $s")
                    exito = Pair(true, null)
                    return@onSuccess
                }
                .onError { code, message ->
                    exito =
                        if (code == HttpCodes.ERROR_204_NOT_CONTENT) Pair(true, null)
                        else Pair(false, message)
                    Log.w("isTokenValid", "onError: $message")
                }
                .onException { ex ->
                    exito = Pair(false, ex.message)
                    Log.w("isTokenValid", "onException: ${ex.message}")
                }

        }?: kotlin.run { exito = Pair(false, "No se ha encontrado el token") }

        return exito
    }

    /**
     * Invalida el token y lo elimina del Account Manager
     * @param account Cuenta a la que pertenece el token
     * @return Pair<Boolean, String?>?
     *    true: si se ha invalidado el token
     *    false: si no se ha podido invalidar el token
     *    String: mensaje de error en caso de que no se haya podido invalidar el token
     */
    suspend fun logOut (account: Account): Pair<Boolean, String?>? {
        var exito : Pair<Boolean, String?>? = null
        val token = am.getToken(account)
        token?.let {
            authService.logOut("Bearer $it")
                .onSuccess {
                    Log.w("logOut", "onSuccess: $it")
                    am.clearToken(account)
                    exito = Pair(true, null)
                }
                .onError { code, message ->
                    exito = Pair(false, message)
                    Log.w("logOut", "code $code onError: $message")
                }
                .onException { ex ->
                    exito = Pair(false, ex.message)
                    Log.w("logOut", "onException: ${ex.message}")
                }
        } ?: run { exito = Pair(false, "No se ha encontrado el token") }

        return exito
    }

    /**
     * Refresca el token de autenticación y lo guarda en el Account Manager
     * @param account Cuenta a la que pertenece el token
     * @return Pair<Boolean, String?>?
     *   true: si se ha refrescado el token
     *   false: si no se ha podido refrescar el token
     *   String: mensaje de error en caso de que no se haya podido refrescar el token
     */
    suspend fun refreshToken (account: Account): Pair<Boolean, String?>? {
        var exito : Pair<Boolean, String?>? = null
        val token = am.getToken(account).orEmpty()
        val refreshToken = am.getRefreshToken(account)

        authService.refreshAuthToken("Bearer $token", refreshToken)
            .onSuccess {
                Log.w("refreshToken", "onSuccess: $it")
                am.setAuthResponse(it, account)
                am.setToken(account, it.accessToken)
                exito = Pair(true, null)
            }
            .onError { code, message ->
                exito = Pair(false, message)
                Log.w("refreshToken", "code $code onError: $message")
            }
            .onException { ex ->
                exito = Pair(false, ex.message)
                Log.w("refreshToken", "onException: ${ex.message}")
            }
        return exito
    }

}

