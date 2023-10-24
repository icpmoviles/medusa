package es.icp.medusa.repositorio

import android.accounts.Account
import android.accounts.AccountManager
import com.google.gson.GsonBuilder
import es.icp.medusa.data.remote.modelos.AuthRequest
import es.icp.medusa.data.remote.modelos.AuthResponse
import es.icp.medusa.data.remote.service.AuthService
import es.icp.medusa.utils.*
import org.apache.commons.codec.binary.Base64

class AuthRepo constructor(
    private val authService: AuthService,
    private val am: AccountManager
) {

    /**
     * Obtiene el token de Perseo y lo guarda en el AccountManager ya sea creando una nueva cuenta o
     * actualizando la cuenta existente
     * @throws MedusaAuthException si no se ha podido obtener el token
     * @throws MedusaAuthExceptionNoDataFound si la llamada ha sido satisfactoria pero no se han obtenido datos
     * @param request AuthRequest (username, password)
     * @return Boolean
     *      true: si se ha obtenido el token
     *      false: si no se ha obtenido el token
     *      String: mensaje de error en caso de que no se haya obtenido el token
     */
    suspend fun getTokenPerseo(request: AuthRequest): AuthResponse? {

        val response = authService.getTokenPerseo(request)

        return if (response.isSuccessful) {
            var authResponseReturnable: AuthResponse? = null
            response.body()?.let { authResponse ->
                val gson = GsonBuilder().serializeNulls().create()
                val json = gson.toJson(authResponse)
                val loginBase64String = String(Base64.encodeBase64(json.toByteArray()))

                val guidResponse = authService.GenerateSecurityGuidFromCredentials("Bearer " + authResponse.accessToken, loginBase64String)
                authResponse.guid = guidResponse
                authResponseReturnable = authResponse

                val cuenta = am.getAccountByName(request.username)
                cuenta?.let {
                    am.setAuthResponse(authResponse, it)
                } ?: run {
                    am.createAccount(
                        username = request.username,
                        password = request.password,
                        authResponse = authResponse
                    )
                }
            } ?: run {
                throw MedusaAuthExceptionNoDataFound("La llamada fue satisfactoria pero no se han obtenido datos")
            }
            return authResponseReturnable
        } else {
            throw MedusaAuthException("Ocurrió un error al solicitar el token (${response.message()})")
        }
    }

    /**
     * Comprueba si el token es válido contra el servidor
     * @throws MedusaAuthException si no se ha podido comprobar el token
     * @throws MedusaAuthExceptionNoDataFound si la llamada ha sido satisfactoria pero no se han obtenido datos
     * @return Boolean
     *     true: si el token es válido
     *     false: si el token no es válido
     *     String: mensaje de error en caso de que no se haya podido comprobar el token
     */
    suspend fun isTokenValid(account: Account): Boolean {

        am.getToken(account)?.let { token ->

            val response = authService.isTokenValid("Bearer $token")

            //Esta llamada puede dar un 204 y lo tenemos que considerar como un token válido
            return if (response.isSuccessful) {
                response.code() == 200 || response.code() == 204
            } else {
                throw MedusaAuthException("Ocurrió un error al validar el token (${response.message()})")
            }
        } ?: run {
            throw MedusaAuthExceptionNoDataFound("No se ha encontrado el token o no esta disponible")
        }
    }

    /**
     * Invalida el token y lo elimina del Account Manager
     * @param account Cuenta a la que pertenece el token
     * @throws MedusaAuthException si no se ha podido invalidar el token
     * @throws MedusaAuthExceptionNoDataFound si la llamada ha sido satisfactoria pero no se han obtenido datos
     * @return Boolean
     *    true: si se ha invalidado el token
     *    false: si no se ha podido invalidar el token
     *    String: mensaje de error en caso de que no se haya podido invalidar el token
     */
    suspend fun logOut(account: Account): Boolean {

        am.getToken(account)?.let { token ->

            val response = authService.logOut("Bearer $token")

            return if (response.isSuccessful) {
                am.clearToken(account)
                true
            } else {
                throw MedusaAuthException("Ocurrió un error al invalidar el token (${response.message()})")
            }

        } ?: run {
            throw MedusaAuthExceptionNoDataFound("No se ha encontrado el token o no esta disponible")
        }
    }

    /**
     * Refresca el token de autenticación y lo guarda en el Account Manager
     * @param account Cuenta a la que pertenece el token
     * @throws MedusaAuthException si no se ha podido refrescar el token
     * @throws MedusaAuthExceptionNoDataFound si la llamada ha sido satisfactoria pero no se han obtenido datos
     * @return Boolean
     *   true: si se ha refrescado el token
     *   false: si no se ha podido refrescar el token
     *   String: mensaje de error en caso de que no se haya podido refrescar el token
     */
    suspend fun refreshToken(account: Account): Boolean {

        am.getToken(account)?.let { token ->

            val refreshToken = am.getRefreshToken(account)

            val response = authService.refreshAuthToken("Bearer $token", refreshToken)

            return if (response.isSuccessful) {

                response.body()?.let { authResponse ->
                    am.setAuthResponse(authResponse, account)
                    am.setToken(account, authResponse.accessToken)
                } ?: run {
                    throw MedusaAuthExceptionNoDataFound("La llamada fue satisfactoria pero no se han obtenido datos")
                }

                true
            } else {
                throw MedusaAuthException("Ocurrió un error al refrescar el token (${response.message()})")
            }

        } ?: run {
            throw MedusaAuthExceptionNoDataFound("No se ha encontrado el token o no esta disponible")
        }
    }
}

class MedusaAuthException(message: String?) : Exception(message)
class MedusaAuthExceptionNoDataFound(message: String?) : Exception(message)

