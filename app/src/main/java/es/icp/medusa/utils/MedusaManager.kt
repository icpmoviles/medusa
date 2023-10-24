package es.icp.medusa.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.util.Log
import es.icp.medusa.data.remote.modelos.AuthRequest
import es.icp.medusa.data.remote.modelos.AuthResponse
import es.icp.medusa.repositorio.AuthRepo
import es.icp.medusa.repositorio.MedusaAuthException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedusaManager @Inject constructor(
    private val authRepo: AuthRepo,
    private val application: Application
) {

    private val TAG = "MedusaManager"

    fun getActiveGUIDForBlazorLoad(): Flow<MedusaActiveGUIDState> = flow {
        Log.d(TAG, "getActiveGUIDForBlazorLoad: MedusaActiveGUIDState.Loading")
        emit(MedusaActiveGUIDState.Loading)
        try {
            val accountManager = AccountManager.get(application)
            val account = accountManager.getActiveAccountByPackageName("es.icp.perseolauncher")
            Log.d(TAG, "getActiveGUIDForBlazorLoad: account: $account")
            if (account == null) {
                Log.d(TAG, "getActiveGUIDForBlazorLoad: MedusaActiveGUIDState.NotFound")
                emit(MedusaActiveGUIDState.NotFound)
                return@flow
            }

            val response = authRepo.isTokenValid(account)
            Log.d(TAG, "getActiveGUIDForBlazorLoad: response: $response")

            if (response) {
                val perseoAccount = accountManager.getAuthResponse(account)
                Log.d(TAG, "getActiveGUIDForBlazorLoad: perseoAccount: $perseoAccount")
                perseoAccount?.let {
                    Log.d(TAG, "getActiveGUIDForBlazorLoad: MedusaActiveGUIDState.Success")
                    emit(MedusaActiveGUIDState.Success(perseoAccount, account))
                }
                return@flow
            }

            val authRequest = accountManager.getAuthRequest(account)
            Log.d(TAG, "getActiveGUIDForBlazorLoad: authRequest: $authRequest")
            val login = authRepo.getTokenPerseo(authRequest)
            Log.d(TAG, "getActiveGUIDForBlazorLoad: login: $login")
            login?.let {
                Log.d(TAG, "getActiveGUIDForBlazorLoad: MedusaActiveGUIDState.Success")
                emit(MedusaActiveGUIDState.Success(login, account))
            }?: run {
                Log.d(TAG, "getActiveGUIDForBlazorLoad: MedusaActiveGUIDState.Error")
                emit(MedusaActiveGUIDState.Error(MedusaAuthException("Error al obtener el login")))
            }

        } catch (e: Exception) {
            Log.d(TAG, "getActiveGUIDForBlazorLoad: MedusaActiveGUIDState.Error")
            e.printStackTrace()
            emit(MedusaActiveGUIDState.Error(e))
        }
    }

}



sealed class LoginState {
    object Loading: LoginState()
    data class Success(val auth: AuthResponse): LoginState()
    data class Error (val error: Throwable): LoginState()
}

sealed class MedusaActiveGUIDState {
    object Loading: MedusaActiveGUIDState()
    data class Success(val auth: AuthResponse, val account: Account): MedusaActiveGUIDState()
    object NotFound: MedusaActiveGUIDState()
    data class Error(val error: Throwable): MedusaActiveGUIDState()
}