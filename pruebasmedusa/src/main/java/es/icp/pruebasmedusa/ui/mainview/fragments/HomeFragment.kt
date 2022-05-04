package es.icp.pruebasmedusa.ui.mainview.fragments

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import es.icp.medusa.authenticator.clearToken
import es.icp.medusa.ui.IntroActivity
import es.icp.pruebasmedusa.R
import es.icp.pruebasmedusa.databinding.FragmentHomeBinding
import es.icp.pruebasmedusa.ui.mainview.MainActivity


class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentAccount: Account
    private lateinit var am: AccountManager

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

        binding.btnLogOut.setOnClickListener{ logOut() }
    }

    fun logOut() {

        am.clearToken(currentAccount)
        requireActivity().finish()
        startActivity(Intent(requireContext(), IntroActivity::class.java))
    }
}