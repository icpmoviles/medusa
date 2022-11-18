package es.icp.medusa.data.remote.repository

import es.icp.medusa.data.remote.service.AuthService
import es.icp.medusa.data.remote.modelos.request.LoginRequest
import es.icp.medusa.data.remote.modelos.response.AuthResponse
import es.icp.medusa.data.remote.modelos.response.UsuarioResponse
import es.icp.medusa.utils.AuthEndPoints
import retrofit2.Response

class AuthRepository constructor(private val authService: AuthService) {

    suspend fun doLogin (request: LoginRequest): Response<AuthResponse> =
        authService.doLogin(AuthEndPoints.ENDPOINT_LOGIN, request)

    suspend fun getUserDataFromServer (): Response<UsuarioResponse> =
        authService.getUserDataFromServer(AuthEndPoints.ENDPOINT_USERS)

    suspend fun isTokenValid (): Response<*> =
        authService.isTokenValid(AuthEndPoints.ENDPOINT_ISTOKENVALID)

    suspend fun logOut (): Response<Void> =
        authService.logOut(AuthEndPoints.ENDPOINT_LOGOUT)

    suspend fun refreshAuthToken (icprt: String): Response<AuthResponse> =
        authService.refreshAuthToken(AuthEndPoints.ENDPOINT_REFRESH_TOKEN, icprt)
}