package es.icp.medusa.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.gson.Gson
import es.icp.medusa.AuthViewModel
import es.icp.medusa.authenticator.*
import es.icp.medusa.databinding.ActivityAuthBinding
import es.icp.medusa.data.remote.modelos.request.LoginRequest
import es.icp.medusa.data.remote.modelos.response.AuthResponse
import es.icp.medusa.utils.*
import java.util.*

class AuthActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAuthBinding
    private lateinit var context: Context
    private lateinit var am : AccountManager
    private var nameAccount: String = ""
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
        setUpView()
    }

    private fun setUpView() {
        supportActionBar?.hide()
        nameAccount = intent.getStringExtra(KEY_NAME_ACCOUNT) ?: ""
        setListeners()
        am = AccountManager.get(context)
        if (nameAccount.isNotBlank()){
            binding.txtuserNameLogin.texto(nameAccount)
            binding.txtpasswordLogin.requestFocus()
        }

        authViewModel.errorService.observe(this){
            hideMeLoader()
            it.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

    }

    private fun setListeners() = with(binding){

        txtuserNameLogin.doOnTextChanged { text, _, _, _ ->
            if (TextUtils.isEmpty(text))
                layoutUserNameLogin.error = "El usuario no puede estar vacío."
            else
                layoutUserNameLogin.error = null
        }
        txtpasswordLogin.doOnTextChanged { text, _, _, _ ->
            if (TextUtils.isEmpty(text))
                layoutPasswordLogin.error = "La contraseña no puede estar en vacía."
            else
                layoutPasswordLogin.error = null
        }

        btnLogin.setOnClickListener {

            it.hideKeyBoard()
            val user = txtuserNameLogin.text.toString().trimEnd()
            val pass = txtpasswordLogin.text.toString().trimEnd()

            when  {
                TextUtils.isEmpty(user) -> {
                    layoutUserNameLogin.error = "El usuario no puede estar vacío."
                    txtuserNameLogin.requestFocus()
                    return@setOnClickListener
                }
                TextUtils.isEmpty(pass) -> {
                    layoutPasswordLogin.error = "La contraseña no puede estar vacía."
                    txtpasswordLogin.requestFocus()
                    return@setOnClickListener
                }
            }

            showMeLoader()
            tryLogin(user, pass)

        }
    }

    private fun showMeLoader() = with(binding){
        imageLoader.apply {
            visible()
            rotateYForever()
        }
        viewDisable.visible()
        containerLogin.createBlur()

    }

    private fun hideMeLoader () = with(binding) {
        imageLoader.hide()
        viewDisable.hide()
        containerLogin.removeBlur()
    }


    private fun tryLogin(user: String, pass: String) {

        val passB64 = Base64.encodeToString(pass.toByteArray(), Base64.NO_WRAP)
        val request = LoginRequest(user, passB64)

        authViewModel.doLogin(request) { result ->
            result?.let {
                finishLogin(
                    authResponse = it,
                    user = user,
                    pass = pass
                )
            }
        }
    }

    private fun finishLogin (authResponse: AuthResponse, user: String, pass: String) {
        authViewModel.getUserDataFromServer {

            it?.let {
                // login correcto -> creacion de cuenta
                // aplicamos a la respuesta el tiempo de expiracion
                authResponse.dateExpire = Date().addSeconds((authResponse.expiresIn - 100))
                //Creamos la cuenta y el bundle de datos de usario(contendra el response)
                val account = Account(user, MY_ACCOUNT_TYPE)
                val userData = Bundle()
                //configuramos el tiempo de caducidad del token
                Log.w("alarm", "${authResponse.dateExpire}")
                setAlarm(authResponse.dateExpire.time)
                //añadimos el response en json al bundle
                userData.putString(KEY_USERDATA_TOKEN, Gson().toJson(authResponse))

                userData.putString(KEY_USERDATA_INFO, Gson().toJson(it))
                // borramos la cuenta si ya existe
                am.removeAccountExplicitly(account)
                // creamos la cuenta
                am.addAccountExplicitly(account, pass, userData)
                // le metemos el token a la cuenta
                am.setAuthToken(account, MY_AUTH_TOKEN_TYPE, authResponse.accessToken)

//                PasswordStorageHelper(context).setData("userName", user.toByteArray())

                // creamos bundle de respuesta con la cuenta loggeada
                val bundle = Bundle().apply {
                    this.putParcelable(KEY_BUNDLE_ACCOUNT, account)
                }
                // añadimos el bundle con la cuenta al intent
                val intent = Intent().also { mIntent ->
                    mIntent.putExtra(KEY_BUNDLE_ACCOUNT, bundle)
                }


                // mandamos el resultado de vuelta
                // no hace falta mandar resultado si no ha sido
                //satisfactorio el login, ya se controla esto a la vuelta
                setResult(RESULT_OK, intent)
                finish()
            }?: kotlin.run { Toast.makeText(context, "Se ha pruducido un error desconocido", Toast.LENGTH_LONG) }

        }
    }

    private fun setAlarm(time: Long) {
        Log.w("primer alarma", "se ejecuto")
        //obteniendo el administrador de alarmas
        val am = getSystemService(ALARM_SERVICE) as AlarmManager

        //creando una nueva intención especificando el receptor de transmisión
        val i = Intent(this, AlarmReciever::class.java).putExtra(KEY_NAME_ACCOUNT, nameAccount )

        //creando una intención pendiente usando la intención
        //configurar la alarma que se activará cuando expire el token
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT) // estab en 0
            am.set(AlarmManager.RTC,time,pi)
        }
        else {
            val pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_MUTABLE)
            am.set(AlarmManager.RTC,time,pi)
        }

        //configurar la alarma que se activará cuando expire el token
//        am.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pi)

//        Toast.makeText(this, "La alarma está configurada", Toast.LENGTH_SHORT).show()
    }


}