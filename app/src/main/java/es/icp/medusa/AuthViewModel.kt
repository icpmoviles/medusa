package es.icp.medusa

import android.accounts.AccountManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.icp.medusa.authenticator.getAuthToken
import es.icp.medusa.data.remote.repository.AuthRepository
import es.icp.medusa.data.remote.service.AuthService
import es.icp.medusa.data.remote.modelos.request.LoginRequest
import es.icp.medusa.data.remote.modelos.response.AuthResponse
import es.icp.medusa.data.remote.modelos.response.UsuarioResponse
import es.icp.medusa.utils.GlobalVariable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.concurrent.TimeUnit

class AuthViewModel  : ViewModel() {


    private val client: OkHttpClient = OkHttpClient.Builder().apply {
        readTimeout(1, TimeUnit.MINUTES)
        writeTimeout(1, TimeUnit.MINUTES)
        connectTimeout(1, TimeUnit.MINUTES)
        interceptors().add(Interceptor {
            val request = it.request().newBuilder()
//            .addHeader(
//                "Content-type:", "application/json; charset=UTF-8"
//            )
//            .addHeader("Accept", "*/*")
                .addHeader("Authorization", "Bearer ${GlobalVariable.accessToken}")
                .addHeader("ih", "H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8=")
                .build()

            return@Interceptor it.proceed(request)
        })
    }.build()
    private val service: AuthService = AuthService.getInstance(client)
    private val repository: AuthRepository = AuthRepository(service)

    private var _loginSuccessfull = MutableLiveData<AuthResponse>()
    val loginSuccessfull: LiveData<AuthResponse> get() = _loginSuccessfull


    private var _errorService = MutableLiveData<String?>()
    val errorService : LiveData<String?> get() = _errorService
    fun setValueErrorService(errorMessage: String?) = _errorService.postValue(errorMessage)

    fun doLogin(request: LoginRequest, result: (AuthResponse?) -> Unit) {
        viewModelScope.launch {

            val response = repository.doLogin(request)
            Log.w("Auht Respondse", response.toString())

            if (response.isSuccessful) {
                when (response.code()){
                    200 -> {
                        val data = response.body()
                        data?.let {
                            _loginSuccessfull.postValue(it)
                            result.invoke(it)
                            GlobalVariable.accessToken = it.accessToken
                        }
                        Log.w("token", GlobalVariable.accessToken.toString())
                    }
                    else -> _errorService.postValue("Se ha producido un error ${response.code()} en el servidor")
                }


            } else _errorService.postValue("Error al iniciar sesión. Revise los datos introducidos.")
        }
    }


    fun getUserDataFromServer(result: (UsuarioResponse?) -> Unit) {
        viewModelScope.launch {
            val response = repository.getUserDataFromServer()
            Log.w("getUserDataFromServer", response.toString())

            Log.w("getUserDataFromServer", response.body().toString())
            if (response.isSuccessful) {
                when (response.code()){
                    200 -> {
                        val data = response.body()
                        data?.let { result.invoke(it) }
                    }
                    else -> _errorService.postValue("Se ha producido un error ${response.code()} en el servidor")
                }


            } else _errorService.postValue("Se ha producido un error ${response.code()} en el servidor")
        }
    }

    // TODO ojo porque el sistema de auto refresh se ejecuta cuando se recibe un 401
    // TODO usar con precaucion, Mejor usar account manager
    fun isTokenValid (result: (Boolean) -> Unit) {
        viewModelScope.launch {
            val response = repository.isTokenValid()
            Log.w("isTokenValid", response.toString())

            if (response.isSuccessful) {
                when (response.code()) {
                    200 -> {
                        Log.w("isTokenValid", response.body().toString())
                        val data = response.body()
                        data?.let { result.invoke(it as Boolean) }
                    }
                    else -> {
                        result.invoke(false)
                        _errorService.postValue("Se ha producido un error ${response.code()} en el servidor")
                    }
                }

            } else {
                result.invoke(false)
                _errorService.postValue("Se ha producido un error ${response.code()} en el servidor")
            }
        }
    }

    fun logOut (result: (Boolean) -> Unit) {
        viewModelScope.launch {
            val response = repository.logOut()
            Log.w("logOut", response.toString())

            if (response.isSuccessful) {
                when (response.code()) {
                    200 -> {
                        Log.w("logOut", response.toString())
//                        val data = response.body()
//                        data?.let { }
                        result.invoke(true)
                    }
                    else -> {
                        result.invoke(false)
                        _errorService.postValue("Se ha producido un error ${response.code()} en el servidor")
                    }
                }

            } else {
                result.invoke(false)
                _errorService.postValue("Se ha producido un error ${response.code()} en el servidor")
            }
        }
    }

    fun refreshAuthToken(icprt: String, result: (AuthResponse?) -> Unit) {
        viewModelScope.launch {
            val response = repository.refreshAuthToken(icprt)

            Log.w("Auht Respondse", response.toString())
            Log.w("Auht Respondse", response.raw().toString())

            if (response.isSuccessful) {
                when (response.code()){
                    200 -> {
                        val data = response.body()
                        data?.let {
                            _loginSuccessfull.postValue(it)
                            result.invoke(it)
                            GlobalVariable.accessToken = it.accessToken
                        }
                        Log.w("token", GlobalVariable.accessToken.toString())
                    }
                    else -> _errorService.postValue("Se ha producido un error ${response.code()} en el servidor")
                }


            } else _errorService.postValue("Error al iniciar sesión. Revise los datos introducidos.")
        }
    }
}