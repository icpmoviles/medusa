package es.icp.pruebasmedusa.ui.mainview.fragments

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import es.icp.medusa.authenticator.*
import es.icp.medusa.modelo.TokenResponse
import es.icp.medusa.repo.WebServiceLogin
import es.icp.pruebasmedusa.databinding.FragmentHomeBinding
import es.icp.pruebasmedusa.ui.mainview.MainActivity


class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentAccount: Account
    private lateinit var am: AccountManager
    private lateinit var tokenResponse: TokenResponse

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
        am = AccountManager.get(requireContext())
        (activity as AppCompatActivity).supportActionBar?.title = "Home Fragment"
        //usar variable del activity padre
        currentAccount = (activity as? MainActivity)?.account!!
        binding.txt.text = currentAccount.toString()


        tokenResponse =
            Gson().fromJson(am.getUserData(currentAccount, KEY_USERDATA_TOKEN), TokenResponse::class.java)



        binding.btnLogOut.setOnClickListener{ logOut() }
        binding.btnIsTokenValid.setOnClickListener {
            am.isTokenValidFromServer(
                requireContext(),
                currentAccount
            ){
                if (it)
                    Log.w("HOME FRAGMENT isvalid", it.toString())
                else
                    Log.w("HOME FRAGMENT isNOTvalid", it.toString())
            }


        }
        binding.btnRefreshToken.setOnClickListener{
            am.refreshToken(
                requireContext(),
                currentAccount
            ){}

        }

        binding.btnInfo.setOnClickListener {
            Log.w("token::", am.getTokenResponse(currentAccount).toString())
            Log.w("user:::", am.getUserDataResponse(currentAccount).toString())
        }
    }

    fun logOut() {

        am.removeTokenAccount(currentAccount)


        WebServiceLogin.invalidateToken(
            requireContext(),
            tokenResponse.accessToken
        ){
            am.clearToken(currentAccount)
            if (it) {
            }
            Log.w("SALIENDO LOGIN", it.toString())
        }
//        requireActivity().finish()
//        startActivity(Intent(requireContext(), IntroActivity::class.java))
    }
}