package es.icp.medusa.authenticator

import android.accounts.AccountManager
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.google.gson.Gson
import es.icp.medusa.R
import es.icp.medusa.repo.WebServiceLogin
import es.icp.medusa.ui.IntroActivity
import es.icp.medusa.utils.Dx

class AlarmReciever: BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = 100
        const val NOTIFICATION_CHANNEL_ID = "1000"
    }
    var nameAccount : String? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent) {
        val am = AccountManager.get(context)
        nameAccount = intent.getStringExtra(KEY_NAME_ACCOUNT) ?: ""
        nameAccount?.let { name ->
            val account = am.getAccountByName(name)
            createNotificationChannel(context)


            if (appInForeground(context)) {
                Log.w("::::", "APP EN PRIMER PLANO")
                account?.let {
                    am.getToken(it)?.let { token ->
                        val successfull = am.refreshToken(
                            context,
                            it
                        )
                        Log.w("suces", successfull.toString())
                        if (successfull)
                            setAlarm(context, (am.getTimeExpire(it).toLong() - 100))
                        else {
                            notifyNotification(context, name)
//                            Log.w("no suces", "refresh fail")
//                            context.startActivity(
//                                Intent(context, IntroActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                            )
                        }
                    } ?: kotlin.run {

//                        context.startActivity(
//                            Intent(context, IntroActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                        )
                        notifyNotification(context, name)
                    }
                }
            }
            else {
                account?.let {
                    am.clearToken(it)
                }
                notifyNotification(context, name)

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

    fun setAlarm(context: Context, time: Long) {
        Log.w("setalarm", "configurada nueva alarma")
        //obteniendo el administrador de alarmas
        val am = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

        //creando una nueva intención especificando el receptor de transmisión
        val i = Intent(context, AlarmReciever::class.java).putExtra(KEY_NAME_ACCOUNT, nameAccount )

        //creando una intención pendiente usando la intención
        //configurar la alarma que se activará cuando expire el token
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT) // estab en 0
            am.set(AlarmManager.RTC,time,pi)
        }
        else {
            val pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_MUTABLE)
            am.set(AlarmManager.RTC,time,pi)
        }

        //configurar la alarma que se activará cuando expire el token
//        am.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pi)

//        Toast.makeText(this, "La alarma está configurada", Toast.LENGTH_SHORT).show()
    }
}