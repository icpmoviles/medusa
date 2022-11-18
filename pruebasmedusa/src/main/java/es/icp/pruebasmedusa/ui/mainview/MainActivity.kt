package es.icp.pruebasmedusa.ui.mainview

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import es.icp.medusa.authenticator.*
import es.icp.medusa.data.remote.modelos.response.AuthResponse
import es.icp.medusa.data.remote.modelos.response.UsuarioResponse
import es.icp.pruebasmedusa.R
import es.icp.pruebasmedusa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var context: Context
    private lateinit var navController: NavController
    lateinit var account: Account
    lateinit var accountManager: AccountManager
    lateinit var usuarioResponse: UsuarioResponse
    lateinit var authResponse: AuthResponse

    val result: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            accountManager = AccountManager.get(context)
            account =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    intent.getParcelableExtra(KEY_BUNDLE_ACCOUNT, Account::class.java)!!
                else
                    intent.getParcelableExtra(KEY_BUNDLE_ACCOUNT)!!
            usuarioResponse = accountManager.getUsusarioResponse(account)
            authResponse = accountManager.getAuthResponse(account)
            Log.w("Activity Result ,main->", accountManager.getAuthResponse(account).toString())

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
        accountManager = AccountManager.get(context)
        setUpView()
    }


    private fun setUpView() {
        val navHostManager = supportFragmentManager.findFragmentById(R.id.navHostMain) as NavHostFragment
        navController = navHostManager.navController
        navController.setGraph(R.navigation.graph_nav_main)
        setupActionBarWithNavController(navController)

        account = intent.getParcelableExtra(KEY_BUNDLE_ACCOUNT)!!
        usuarioResponse = accountManager.getUsusarioResponse(account)
        authResponse = accountManager.getAuthResponse(account)

        val dataToken = accountManager.getUserData(account, KEY_USERDATA_TOKEN)
        val dataUser = accountManager.getUserData(account, KEY_USERDATA_INFO)

        Log.w("mainActivity", "account -> $account")
        Log.w("dataToken", "data token -> $authResponse")
        Log.w("dataToken", "data user -> $usuarioResponse")

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}