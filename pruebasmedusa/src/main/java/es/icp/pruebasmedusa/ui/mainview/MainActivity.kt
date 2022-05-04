package es.icp.pruebasmedusa.ui.mainview

import android.accounts.Account
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import es.icp.medusa.authenticator.KEY_BUNDLE_ACCOUNT
import es.icp.pruebasmedusa.R
import es.icp.pruebasmedusa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var context: Context
    private lateinit var navController: NavController
    lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this

        setUpView()
    }


    private fun setUpView() {
        val navHostManager = supportFragmentManager.findFragmentById(R.id.navHostMain) as NavHostFragment
        navController = navHostManager.navController
        navController.setGraph(R.navigation.graph_nav_main)
        setupActionBarWithNavController(navController)

        account = intent.getParcelableExtra(KEY_BUNDLE_ACCOUNT)!!
        Log.w("mainActivity", "account -> $account")
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}