package es.icp.pruebasmedusa.ui.mainview

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import es.icp.medusa.data.remote.modelos.AuthRequest
import es.icp.medusa.data.remote.modelos.AuthResponse
import es.icp.medusa.data.remote.service.AuthService
import es.icp.medusa.utils.*
import es.icp.pruebasmedusa.BuildConfig
import es.icp.pruebasmedusa.R
import es.icp.pruebasmedusa.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var context: Context
    private lateinit var navController: NavController
    lateinit var account: Account
    lateinit var accountManager: AccountManager
    lateinit var authResponse: AuthResponse

    private val vm : MainViewModel by viewModels()


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

//        vm.getAuthToken("https://ticketingicp.icp.es:9013/" + AuthService.ENDPOINT_LOGIN, AuthRequest("icp.5241","Temporal2."))

        lifecycleScope.launch {
            accountManager.getMyAccounts().forEach {
                Log.w("MainActivity", "Account: ${it.name} - ${it.type}")
            }

            vm.getTokenPerseo(AuthRequest("icp.5241","Temporal2."))
            val ac = accountManager.getAccountByName("icp.5241")
            accountManager.setActiveAccountByPackageName(ac!!, BuildConfig.APPLICATION_ID)
            delay(5000)
            val account = accountManager.getAccountByName("icp.12830")

            val mapa : HashMap<String, Boolean> = HashMap()


            Log.w("MainActivity", "Account Active: ${accountManager.getActiveAccountByPackageName(BuildConfig.APPLICATION_ID)}")

//            vm.isTokenValid(account!!)
//            delay(2000)
//            vm.refreshToken(account)
//            delay(2000)
//            vm.isTokenValid(account)


            Log.w("MainActivity", "Active Account: ${ac?.name} - ${ac?.type}")
            Log.w("MainActivity", "packagename: ${applicationContext.packageName}")
            val boo = accountManager.setAccountVisibility(account, context.packageName, AccountManager.VISIBILITY_NOT_VISIBLE)
//            delay(2000)
//            vm.logOut(account)
//            delay(2000)
//            vm.isTokenValid(account)
            Log.w("MainActivity", "Account Visibility: $boo")

            delay(2000)
            accountManager.setAccountVisibility(ac, context.packageName, AccountManager.VISIBILITY_NOT_VISIBLE)
            accountManager.getAccountsByTypeForPackage( ConstantesAuthPerseo.MY_ACCOUNT_TYPE, applicationContext.packageName).forEach {
                Log.w("MainActivity", "Account: ${it.name} - ${it.type}")
            }

        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}