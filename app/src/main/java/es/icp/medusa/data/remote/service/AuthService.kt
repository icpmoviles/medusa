package es.icp.medusa.data.remote.service

import es.icp.genericretrofit.utils.GenericResponse
import es.icp.medusa.data.remote.modelos.AuthRequest
import es.icp.medusa.data.remote.modelos.AuthResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio de autenticación de PERSEO
 * Las aplicaciones que vayan bajo PERSEO deben implementar este servicio para poder autenticarse
 */
interface AuthService {
    companion object{
        const val ENDPOINT_LOGIN = "icpsec/Login"
        const val ENDPOINT_LOGOUT = "icpsec/Logout"
        const val ENDPOINT_IS_TOKEN_VALID = "icpsec/IsTokenValid"
        const val ENDPOINT_REFRESH_TOKEN = "icpsec/RefreshToken"
        const val ENDPOINT_GENERATE_GUID_FROM_CREDENTIALS = "icpsec/GenerateSecurityGuidFromCredentials"
    }

    /**
     * Obtiene el token de autenticación del servidor
     * @param url URL del servidor (tu dominio) + ENDPOINT_LOGIN
     * @param request AuthRequest con el usuario y contraseña
     * @return Response<AuthResponse> con el token de autenticación
     *   200 si el usuario y contraseña son correctos
     *   401 si el usuario y contraseña no son correctos
     */
    @POST(ENDPOINT_LOGIN)
    suspend fun getTokenPerseo (
        @Body request: AuthRequest
    ) : GenericResponse<AuthResponse>

    @Headers("ih: H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8=")
    @FormUrlEncoded
    @POST(ENDPOINT_GENERATE_GUID_FROM_CREDENTIALS)
    suspend fun GenerateSecurityGuidFromCredentials(
        @Header("Authorization") accessToken: String,
        @Field("tfisg") tfisg: String,
    ) : String

    /**
     * Refresca el token de autenticación en el servidor
     * @param url URL del servidor (tu dominio) + ENDPOINT_REFRESH_TOKEN
     * @param icprt token de refresco del usuario
     * @return Response<AuthResponse> con el nuevo token de autenticación
     *    200 si el token se ha refrescado correctamente
     *    401 si el token no es válido
     */
    @POST(ENDPOINT_REFRESH_TOKEN)
    @Headers("ih: H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8=")
    @FormUrlEncoded
    suspend fun refreshAuthToken(
        @Header("Authorization") token: String,
        @Field("icprt") icprt: String
    ): GenericResponse<AuthResponse>

    /**
     * Invalida el token de autenticación en el servidor
     * @param url URL del servidor (tu dominio) + ENDPOINT_LOGOUT
     * @return Response<Void>
     *     200 si el token se ha invalidado correctamente
     *     401 si el token no es válido
     */
    @GET(ENDPOINT_LOGOUT)
    @Headers("ih: H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8=")
    suspend fun logOut(
        @Header("Authorization") token: String
    ): GenericResponse<Unit>

    /**
     * Comprueba si el token de autenticación es válido contra el servidor
     * @param url URL del servidor (tu dominio) + ENDPOINT_IS_TOKEN_VALID
     * @return Response<Void>
     *     204 si el token es válido
     *     401 si el token no es válido
     */
    @GET(ENDPOINT_IS_TOKEN_VALID)
    @Headers("ih: H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8=")
    suspend fun isTokenValid (
        @Header("Authorization") token: String
    ): GenericResponse<Void>
}