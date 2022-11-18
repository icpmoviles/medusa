package es.icp.medusa.authenticator

import android.accounts.Account
import android.accounts.AccountManager
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import es.icp.medusa.R
import es.icp.medusa.utils.Constantes


class AlarmReciever: BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = 100
        const val NOTIFICATION_CHANNEL_ID = "1000"
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent) {
        val am = AccountManager.get(context)
        val nameAccount = intent.getStringExtra(KEY_NAME_ACCOUNT) ?: ""
        if (nameAccount.isNotBlank()){
            val account : Account? = am.getAccountByName(nameAccount)

            if (appInForeground(context)) {
                Log.w("::::", "APP EN PRIMER PLANO")
                account?.let {
                    am.refreshAuthToken(context, it){ result ->
                        when (result){
                            true -> {
                                setAlarm(context, am.getTimeExpire(it).time, nameAccount)
                                Log.w("suces refresh", "token refrescado")
                            }
                            false ->{
                                createNotificationChannel(context)
                                notifyNotification(context, nameAccount)
                                am.clearAuthToken(it)
                            }
                        }

                    }
                } ?: kotlin.run {
                    createNotificationChannel(context)
                    notifyNotification(context, nameAccount)
                }
            }
            else {
                account?.let {
                    am.clearAuthToken(it)
                }
                createNotificationChannel(context)
                notifyNotification(context, nameAccount)

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

    private fun appInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        return runningAppProcesses.any { it.processName == context.packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }

    fun setAlarm(context: Context, time: Long, nameAccount: String) {
        Log.w("setalarm", "configurada nueva alarma")
        //obteniendo el administrador de alarmas
        val am = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

        //creando una nueva intención especificando el receptor de transmisión
        val i = Intent(context, AlarmReciever::class.java).putExtra(KEY_NAME_ACCOUNT, nameAccount )

        //creando una intención pendiente usando la intención
        //configurar la alarma que se activará cuando expire el token
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val pi = PendingIntent.getBroadcast(context, Constantes.REQUEST_CODE_FIN_SESION, i, PendingIntent.FLAG_CANCEL_CURRENT) // estab en 0
            am.set(AlarmManager.RTC,time,pi)
        }
        else {
            val pi = PendingIntent.getBroadcast(context, Constantes.REQUEST_CODE_FIN_SESION, i, PendingIntent.FLAG_MUTABLE)
            am.set(AlarmManager.RTC,time,pi)
        }

        //configurar la alarma que se activará cuando expire el token
//        am.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pi)

//        Toast.makeText(this, "La alarma está configurada", Toast.LENGTH_SHORT).show()
    }


}