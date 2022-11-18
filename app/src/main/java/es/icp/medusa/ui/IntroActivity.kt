package es.icp.medusa.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import es.icp.medusa.AuthViewModel
import es.icp.medusa.authenticator.*
import es.icp.medusa.databinding.ActivityIntroBinding
import es.icp.medusa.utils.GlobalVariable

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var context: Context
    private lateinit var am: AccountManager
    private lateinit var account: Account
    private val authViewModel: AuthViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this

        supportActionBar?.hide()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            setUpView()
        }, 1500)

    }

    private fun setUpView() {

        am = AccountManager.get(context)
        val accounts = am.getMyAccounts()
        //aqui comprobamos si existen cuentas
        if (accounts.isNullOrEmpty()){
            Log.w("splash activity", "no hay cuentas")
            //no hay cuentas, nos vamos a auth sin nombre de cuenta
            authIntent("")
        } else {
            when (accounts.size){
                1 -> {
                    Log.w("splash activity", "hay una cuenta")
                    account = accounts[0]
                    val token = am.getAuthToken(account)
                    token?.let {
                        goToHomeActivity(account)
                        GlobalVariable.accessToken = it

                    }?: kotlin.run { authIntent(account.name) }
//                    am.isTokenValid(account) {
//                        if (it) goToHomeActivity(account)
//                        else authIntent(account.name)
//                    }
                }
                else -> choiceAccountDialog()
            }
        }

    }


    private fun authIntent(nameAccount: String) =
        authLauncher.launch(Intent(applicationContext, AuthActivity::class.java).putExtra(KEY_NAME_ACCOUNT, nameAccount))

    private val authLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == RESULT_OK){
                // recuperar bundle del intent
                val bundle = result.data?.getBundleExtra(KEY_BUNDLE_ACCOUNT)
                // recuperar la cuenta enviada desde authActivity
                val account : Account? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle?.getParcelable(KEY_BUNDLE_ACCOUNT, Account::class.java)
                } else {
                    bundle?.get(KEY_BUNDLE_ACCOUNT)!! as Account
                }
                //enviar la cuenta al activity principal
                account?.let { goToHomeActivity(it) }
            } else finish()
        }

    private fun goToHomeActivity(account: Account){
        Log.w("package", packageName)
        startActivity(
            Intent(Intent.ACTION_MAIN).apply {
                setClassName(packageName, "$packageName.ui.mainview.MainActivity")
                putExtra(KEY_BUNDLE_ACCOUNT, account)
            }
        )
        finish()
    }


    private fun choiceAccountDialog(){
        val accounts = am.getMyAccounts()
        val lista = ArrayList<String>()
        var eleccion = 0

        accounts?.forEach {
            lista.add(it.name)
        }
        val items = lista.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Elige un usuario")
            .setCancelable(false)
            .setSingleChoiceItems(items, -1) { _, i ->
                eleccion = i
            }
            .setPositiveButton("ACEPTAR") { _, _ ->
                account = am.getAccountByName(items[eleccion])!!
                val token = am.getAuthToken(account)
                token?.let {
                    goToHomeActivity(account)
                    GlobalVariable.accessToken = it
                }?: kotlin.run {
                    authIntent(account.name)
                }
            }
            .create()
            .show()
    }

}