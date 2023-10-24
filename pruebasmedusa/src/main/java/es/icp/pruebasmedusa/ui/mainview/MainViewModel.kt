package es.icp.pruebasmedusa.ui.mainview

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import es.icp.genericretrofit.communication.ConnectivityInterceptor
import es.icp.genericretrofit.utils.onError
import es.icp.genericretrofit.utils.onException
import es.icp.genericretrofit.utils.onSuccess
import es.icp.medusa.data.remote.modelos.AuthRequest
import es.icp.medusa.data.remote.service.AuthHeaderInterceptor
import es.icp.medusa.data.remote.service.AuthRetrofit
import es.icp.medusa.data.remote.service.AuthService
import es.icp.medusa.repositorio.AuthRepo
import es.icp.medusa.utils.getAccountByName
import es.icp.medusa.utils.getToken
import es.icp.pruebasmedusa.ui.mainview.repositorio.Repo
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val repo: Repo,
    application: Application
) : AndroidViewModel(application) {

    val am: AccountManager = AccountManager.get(getApplication())

    private val _userName : MutableLiveData<String> = MutableLiveData()
    val userName: LiveData<String> get() = _userName

    var account : Account? = am.getAccountByName(userName.value.orEmpty())
    val token = account?.let { am.getToken(it).orEmpty() }

    private val authService : AuthService = AuthRetrofit.getInstance(
        baseUrl = "https://ticketingicp.icp.es:9013/",
        client =
            OkHttpClient().newBuilder().apply {
                interceptors().add(ConnectivityInterceptor(getApplication<Application>().applicationContext))
//                interceptors().add(AuthHeaderInterceptor(token.orEmpty()))
                readTimeout(1, TimeUnit.MINUTES)
                writeTimeout(1, TimeUnit.MINUTES)
                connectTimeout(1, TimeUnit.MINUTES)
            }.build()
    ).create(AuthService::class.java)
    private val authRepo: AuthRepo = AuthRepo(authService, am)


    fun getTokenPerseo ( request: AuthRequest) = viewModelScope.launch {
        authRepo.getTokenPerseo( request)?.let { pair ->
            if (pair.first){
                Log.w("MainViewModel", "getAuthToken: ${pair.first}")
                _userName.value = request.username
            }
            else
                Log.w("MainViewModel", "getAuthToken: ${pair.second}")
        }

    }

    fun isTokenValid (account: Account) = viewModelScope.launch {
        authRepo.isTokenValid(account)?.let {pair ->
            if (pair.first)
                Log.w("isTokenValid", "isTokenValid: ${pair.first}")
            else
                Log.w("isTokenValid", "isTokenValid: ${pair.second}")
        }
    }

    fun logOut (account: Account) = viewModelScope.launch {
        authRepo.logOut(account)?.let {pair ->
            if (pair.first)
                Log.w("logOut", "logOut: ${pair.first}")
            else
                Log.w("logOut", "logOut: ${pair.second}")
        }
    }

    fun refreshToken (account: Account) = viewModelScope.launch {
        authRepo.refreshToken(account)?.let {pair ->
            if (pair.first)
                Log.w("refreshToken", "refreshToken: ${pair.first}")
            else
                Log.w("refreshToken", "refreshToken: ${pair.second}")
        }
    }
}