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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.gson.Gson
import es.icp.medusa.authenticator.*
import es.icp.medusa.databinding.ActivityAuthBinding
import es.icp.medusa.modelo.TokenRequest
import es.icp.medusa.modelo.TokenResponse
import es.icp.medusa.repo.WebServiceLogin
import java.util.*

class AuthActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAuthBinding
    private lateinit var context: Context
    private lateinit var am : AccountManager
    private var nameAccount: String = ""
    private var account: Account? = null

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
        if (!nameAccount.isNullOrBlank()){
            binding.txtuserNameLogin.texto(nameAccount)
            binding.txtpasswordLogin.requestFocus()
        }


    }

    private fun finishLogin(user: String, pass: String) {

        val passB64 = Base64.encodeToString(pass.toByteArray(), Base64.NO_WRAP)
        val request = TokenRequest(user, passB64)

        WebServiceLogin.doLogin(
            context,
            request)
        {
            it?.let { token->
                callGetUserData(token, user, pass)
            }?: kotlin.run {
                Toast.makeText(context, "Credenciales de acceso incorrectas.", Toast.LENGTH_LONG).show()
            }


        }

    }

    private fun callGetUserData(tokenResponse: TokenResponse, user: String, pass: String) {
        WebServiceLogin.getUserDataFromServer(
            context,
            tokenResponse.accessToken) {
            it?.let {
                // login correcto -> creacion de cuenta
                // aplicamos a la respuesta el tiempo de expiracion
                tokenResponse.dateExpire = Date().addSeconds((tokenResponse.expiresIn - 100))
                //Creamos la cuenta y el bundle de datos de usario(contendra el response)
                val account = Account(user, MY_ACCOUNT_TYPE)
                val userData = Bundle()
                //configuramos el tiempo de caducidad del token
                Log.w("alarm", "${tokenResponse.dateExpire}")
                setAlarm(tokenResponse.dateExpire.time)
                //añadimos el response en json al bundle
                userData.putString(KEY_USERDATA_TOKEN, Gson().toJson(tokenResponse))

                userData.putString(KEY_USERDATA_INFO, Gson().toJson(it))
                // borramos la cuenta si ya existe
                am.removeAccountExplicitly(account)
                // creamos la cuenta
                am.addAccountExplicitly(account, pass, userData)
                // le metemos el token a la cuenta
                am.setAuthToken(account, MY_AUTH_TOKEN_TYPE, tokenResponse.accessToken)

//                PasswordStorageHelper(context).setData("userName", user.toByteArray())

                // creamos bundle de respuesta con la cuenta loggeada
                val bundle = Bundle().apply {
                    putParcelable(KEY_BUNDLE_ACCOUNT, account)
                }
                // añadimos el bundle con la cuenta al intent
                val intent = Intent().also {
                    it.putExtra(KEY_BUNDLE_ACCOUNT, bundle)
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



    private fun setListeners(){
        binding.txtuserNameLogin.doOnTextChanged { text, _, _, _ ->
            if (TextUtils.isEmpty(text))
                binding.layoutUserNameLogin.error = "El usuario no puede estar vacío."
            else
                binding.layoutUserNameLogin.error = null
        }
        binding.txtpasswordLogin.doOnTextChanged { text, _, _, _ ->
            if (TextUtils.isEmpty(text))
                binding.layoutPasswordLogin.error = "La contraseña no puede estar en vacía."
            else
                binding.layoutPasswordLogin.error = null
        }

        binding.btnLogin.setOnClickListener {

            val user = binding.txtuserNameLogin.text.toString().trimEnd()
            val pass = binding.txtpasswordLogin.text.toString().trimEnd()

            when  {
                TextUtils.isEmpty(user) -> {
                    binding.layoutUserNameLogin.error = "El usuario no puede estar vacío."
                    binding.txtuserNameLogin.requestFocus()
                    return@setOnClickListener
                }
                TextUtils.isEmpty(pass) -> {
                    binding.layoutPasswordLogin.error = "La contraseña no puede estar en vacía."
                    binding.txtpasswordLogin.requestFocus()
                    return@setOnClickListener
                }
            }

            finishLogin(user, pass)
        }
    }

}