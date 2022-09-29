package es.icp.medusa.authenticator

import android.accounts.AccountManager
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.gson.Gson
import es.icp.medusa.R
import es.icp.medusa.repo.WebServiceLogin

class AlarmReciever: BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = 100
        const val NOTIFICATION_CHANNEL_ID = "1000"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent) {
        val am = AccountManager.get(context)
        val nameAccount = intent.getStringExtra(KEY_NAME_ACCOUNT) ?: ""
        val account = am.getAccountByName(nameAccount)
        createNotificationChannel(context)
        notifyNotification(context, nameAccount)


        if (appInForeground(context)) {
            Log.w("::::", "APP EN PRIMER PLANO")
            account?.let {
                am.getToken(it)?.let { token ->
                    WebServiceLogin.refreshToken(
                        context,
                        token,
                        am.getRefreshToken(it)
                    ) { tokenResponse ->
//                        am.setAuthToken(account, MY_AUTH_TOKEN_TYPE, tokenResponse!!.accessToken)
//                        am.setUserData(account, KEY_USERDATA_TOKEN, Gson().toJson(tokenResponse) )

                    }
                }
            }
        }
        else {
            account?.let {
                am.clearToken(it)
            }

        }

        Log.w("EEEEEEEEEEEEEEEEE", "AGARRALO FUERTE BROTHER")
    }

    private fun createNotificationChannel(context: Context) {

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Fin Sesion",
            NotificationManager.IMPORTANCE_HIGH
        )

        NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
    }

    private fun notifyNotification(context: Context, accountName: String) {
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("ICP Perseo")
                .setContentText("La sesion del usuario '$accountName' ha expirado.")
                .setSmallIcon(R.drawable.icp_software)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            notify(NOTIFICATION_ID, build.build())

        }

    }

    fun appInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        return runningAppProcesses.any { it.processName == context.packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }


}