package es.icp.medusa.authenticator

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log

class AuthenticatorService: Service() {

    private lateinit var mAuthenticator: Authenticator

    override fun onCreate() {
        // Create a new authenticator object
        Log.d("KK", "PEPOTE")
        mAuthenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return if (intent?.action.equals(AccountManager.ACTION_AUTHENTICATOR_INTENT))
            mAuthenticator.iBinder
        else
            null
    }


    private class Authenticator(context: Context): AbstractAccountAuthenticator(context){


        override fun editProperties(
            response: AccountAuthenticatorResponse?,
            accountType: String?
        ): Bundle {
            return Bundle()
        }

        override fun addAccount(
            response: AccountAuthenticatorResponse?,
            accountType: String?,
            accountTokenType: String?,
            requiredFeatures: Array<out String>?,
            options: Bundle?
        ): Bundle {
            return Bundle()
        }

        override fun confirmCredentials(
            response: AccountAuthenticatorResponse?,
            account: Account?,
            options: Bundle?
        ): Bundle {
            return Bundle()
        }

        override fun getAuthToken(
            response: AccountAuthenticatorResponse?,
            account: Account?,
            authTokenType: String?,
            options: Bundle?
        ): Bundle {
            response?.onResult(Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
            })
            return Bundle()
        }

        override fun getAuthTokenLabel(p0: String?): String {
            return ""
        }

        override fun updateCredentials(
            response: AccountAuthenticatorResponse?,
            account: Account?,
            authTokenType: String?,
            options: Bundle?
        ): Bundle {
            return Bundle()
        }

        override fun hasFeatures(
            response: AccountAuthenticatorResponse?,
            account: Account?,
            features: Array<out String>?
        ): Bundle {
            return Bundle()
        }

        override fun getAccountRemovalAllowed(
            response: AccountAuthenticatorResponse?,
            account: Account?
        ): Bundle {
            return super.getAccountRemovalAllowed(response, account)
        }

    }
}