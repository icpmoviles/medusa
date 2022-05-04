package es.icp.medusa.authenticator

import android.accounts.AccountManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import es.icp.medusa.R

class AlarmReciever: BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = 100
        const val NOTIFICATION_CHANNEL_ID = "1000"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent) {
        val am = AccountManager.get(context)
        val nameAccount = intent.getStringExtra(KEY_NAME_ACCOUNT) ?: ""
        createNotificationChannel(context)
        notifyNotification(context, nameAccount)

        if (am.existsAccountByName(nameAccount)){
            val account = am.getAccountByName(nameAccount)!!
            am.clearToken(account)
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

}