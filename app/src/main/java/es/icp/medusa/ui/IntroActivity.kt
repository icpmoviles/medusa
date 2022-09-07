package es.icp.medusa.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import es.icp.medusa.authenticator.*
import es.icp.medusa.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var context: Context
    private lateinit var am: AccountManager
    private lateinit var account: Account

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
                    if (am.isValidToken(account))
                        goToHomeActivity(account)
                    else
                        authIntent(account.name)
                }
                else -> choiceAccountDialog()
            }
        }

    }


    private fun authIntent(nameAccount: String) =
        authLauncher.launch(Intent(context, AuthActivity::class.java).putExtra(KEY_NAME_ACCOUNT, nameAccount))

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == RESULT_OK){
                // recuperar bundle del intent
                val bundle = result.data?.getBundleExtra(KEY_BUNDLE_ACCOUNT)
                // recuperar la cuenta enviada desde authActivity
                val account = bundle?.get(KEY_BUNDLE_ACCOUNT) as Account
                //enviar la cuenta al activity principal
                goToHomeActivity(account)
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

        AlertDialog.Builder(this)
            .setTitle("Elige un usuario")
            .setCancelable(false)
            .setSingleChoiceItems(items, -1) { _, i ->
                eleccion = i
            }
            .setPositiveButton("ACEPTAR") { _, _ ->
                account = am.getAccountByName(items[eleccion])!!
                if (am.isValidToken(account))
                    goToHomeActivity(account)
                else
                    authIntent(account.name)
            }
            .show()
    }

}