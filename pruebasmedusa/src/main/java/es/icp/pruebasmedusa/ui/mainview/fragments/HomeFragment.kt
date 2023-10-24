package es.icp.pruebasmedusa.ui.mainview.fragments

import android.accounts.Account
import android.accounts.AccountManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import es.icp.medusa.authenticator.AlarmReciever
import es.icp.medusa.data.remote.modelos.AuthResponse
import es.icp.medusa.utils.ConstantesAuthPerseo.KEY_NAME_ACCOUNT
import es.icp.medusa.utils.getAuthResponse
import es.icp.pruebasmedusa.databinding.FragmentHomeBinding
import es.icp.pruebasmedusa.ui.mainview.MainActivity
import es.icp.pruebasmedusa.ui.mainview.MainViewModel


class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentAccount: Account
    private lateinit var am: AccountManager
    private lateinit var authResponse: AuthResponse

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
    }
    private fun setUpView() {

        binding.btnGetGUID.setOnClickListener {
            mainViewModel.getActiveGUI()
        }

//        am = AccountManager.get(requireContext())
//        (activity as AppCompatActivity).supportActionBar?.title = "Home Fragment"
//        //usar variable del activity padre
//        currentAccount = (requireActivity() as MainActivity).account
//        binding.txt.text = currentAccount.toString()
//
//
//        authResponse =
//            Gson().fromJson(am.getUserData(currentAccount, KEY_USERDATA_TOKEN), AuthResponse::class.java)
//
//
//
//        binding.btnLogOut.setOnClickListener{
//            logOut()
//            cancelAlarm(requireContext())
//        }
//        binding.btnIsTokenValid.setOnClickListener {
//            am.isTokenValid(currentAccount) {
//                if (it)
//                    Log.w("HOME FRAGMENT isvalid", it.toString())
//                else
//                    Log.w("HOME FRAGMENT isNOTvalid", it.toString())
//            }
//
//
//        }
//        binding.btnRefreshToken.setOnClickListener{
//            am.refreshAuthToken( requireContext(), currentAccount){
//                Log.w("respose btn re", it.toString())
//            }
//
//        }
//
//        binding.btnInfo.setOnClickListener {
//            Log.w("token::", am.getAuthResponse(currentAccount).toString())
//            Log.w("user:::", am.getUsusarioResponse(currentAccount).toString())
//        }
    }

    fun logOut() {
//        am.clearAuthToken(currentAccount)
//        authViewModel.logOut {
//            Log.w("SALIENDO LOGIN", it.toString())
//        }
    }

    fun cancelAlarm(context: Context){
        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager?
        val i = Intent(requireContext(), AlarmReciever::class.java).putExtra(KEY_NAME_ACCOUNT, currentAccount.name )


            val pi = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(requireContext(), 3141592, i, PendingIntent.FLAG_CANCEL_CURRENT) // estab en 0
            }
            else {
                PendingIntent.getBroadcast(requireContext(), 3141592, i, PendingIntent.FLAG_MUTABLE)
            }

        alarmManager!!.cancel(pi)
    }
}