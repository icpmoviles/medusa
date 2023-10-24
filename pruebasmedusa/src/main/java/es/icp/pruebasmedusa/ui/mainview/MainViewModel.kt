package es.icp.pruebasmedusa.ui.mainview

import android.accounts.Account
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.icp.medusa.data.remote.modelos.AuthRequest
import es.icp.medusa.repositorio.AuthRepo
import es.icp.medusa.utils.MedusaManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application, // esto solo esta por los toast, para no enredar mas a una app de prueba.
    private val authRepo: AuthRepo,
    private val medusaManager: MedusaManager
) : AndroidViewModel(application) {

//    private val accountManager: AccountManager = AccountManager.get(getApplication())
//    private val authRepo: AuthRepo = AuthRepo(NetworkModule().getRetrofit().create(), accountManager)

    fun getTokenPerseo(request: AuthRequest) = viewModelScope.launch {
        try {
            authRepo.getTokenPerseo(request).also {
                Log.d("getTokenPerseo", request.username)
            }
        } catch (e: Exception) {
            Toast.makeText(
                getApplication(),
                "Excepcion detectada mira el logcat para más información.",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    fun getActiveGUI() = viewModelScope.launch(Dispatchers.IO) {
        try {
            medusaManager.getActiveGUIDForBlazorLoad().also {
                Log.d("getActiveGUI", "getActiveGUI: $it")
            }
        } catch (e: Exception) {
            Toast.makeText(
                getApplication(),
                "Excepcion detectada mira el logcat para más información.",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    fun isTokenValid(account: Account) = viewModelScope.launch {
        try {
            authRepo.isTokenValid(account).also { response ->
                Log.w("isTokenValid", "isTokenValid: $response")
            }
        } catch (e: Exception) {
            Toast.makeText(
                getApplication(),
                "Excepcion detectada mira el logcat para más información.",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    fun logOut(account: Account) = viewModelScope.launch {
        try {
            authRepo.logOut(account).also { response ->
                Log.w("logOut", "logOut: $response")
            }
        } catch (e: Exception) {
            Toast.makeText(
                getApplication(),
                "Excepcion detectada mira el logcat para más información.",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    fun refreshToken(account: Account) = viewModelScope.launch {
        try {
            authRepo.refreshToken(account).also { response ->
                Log.w("refreshToken", "refreshToken: $response")
            }
        } catch (e: Exception) {
            Toast.makeText(
                getApplication(),
                "Excepcion detectada mira el logcat para más información.",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }
}