package com.abhi.cowinotpsender

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.abhi.cowinotpsender.injection.NetworkServiceModule.BASE_URL
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject
import android.app.PendingIntent

@AndroidEntryPoint
class ForegroundService : Service() {

    @Inject
    lateinit var retrofit: Retrofit
    private val compositeDisposable = CompositeDisposable()
    lateinit var prefs: SharedPreferences
    private var smsReceiver: SMSBroadcastReceiver? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val  action = intent.getStringExtra(ACTION_KEY)
        if (action == "start") {
            Log.e("abhi", "start")
            smsReceiver = SMSBroadcastReceiver()
            this.registerReceiver(smsReceiver, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
            createNotificationChannel(START_CHANNEL_ID, "OTP Service")
            startForeground(START_ID, startService())

        } else if (action == "cancel" ) {
            Log.e("abhi", "cancel")
            stopForeground(true)
        } else {
            Log.e("abhi", "msg")
            val msg = intent.getStringExtra(MSG)
            try {
                val  otpValue = msg?.substring(37, 43)
                prefs = PreferenceManager.getDefaultSharedPreferences(this)
                createNotificationChannel(CHANNEL_ID, "Sending Otp")
                startForeground(NOTIFY_ID, getNotification("Sending Otp", message = otpValue))
                sendOtp(otpValue)
            } catch (e: Exception) {
                Log.d(packageName, "Error in getting OTP: $e")
            }
        }

        return START_NOT_STICKY
    }



    private fun getNotification(title: String, message: String?, longMsg: String? = null): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("$title and your otp is $message")
            .setSmallIcon(R.drawable.ic_baseline_textsms_24)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText("opt = $message payload = $longMsg"))
            .build()
    }

    private fun sendOtp(otpValue: String?) {
        val url = prefs.getString("url", BASE_URL)
        Log.e("abhi", "url -> $url/$otpValue")
        compositeDisposable.add(
            retrofit.create(SendSmsApi::class.java).sendOtpUrl("$url/$otpValue")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({response -> onResponse(response, otpValue)}, {t -> onFailure(t, otpValue) }))
    }

    private fun updateNotification(text: String, opt: String?, payload:String?) {
        val notification: Notification = getNotification(text, message = opt,  longMsg = payload)
        val mNotificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFY_ID, notification)
    }

    private fun onFailure(t: Throwable, otpValue: String?) {
        Log.e("abhi ", "failed $t")
        updateNotification("Failed sending OTP", otpValue,  t.message)
    }

    private fun onResponse(response: Response<Any>, otpValue: String?) {
        Log.e("abhi ", "response $otpValue body ${response.body()} ${response.code()}")
        if (response.code() == 200) {
            updateNotification("Successfully OTP Sent", opt = otpValue,  response.body().toString())
        } else {
            updateNotification("Failed sending OTP", opt = otpValue, response.body().toString())
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
        stopForeground(true)
        smsReceiver?.let {
            unregisterReceiver(it)
        }
    }

    private fun createNotificationChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun startService(): Notification {

        val notificationIntent = Intent(this, SettingsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        return NotificationCompat.Builder(this, ForegroundService.START_CHANNEL_ID)
            .setContentTitle("Cowin Otp Service Started")
            .setContentText("Service will be stopped if forced closed")
            .setSmallIcon(R.drawable.ic_baseline_textsms_24)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "SendingOtp"
        const val START_CHANNEL_ID = "OtpService"
        const val NOTIFY_ID = 1
        const val START_ID = 2
        const val ACTION_KEY = "action"
        const val MSG = "msg"
    }
}