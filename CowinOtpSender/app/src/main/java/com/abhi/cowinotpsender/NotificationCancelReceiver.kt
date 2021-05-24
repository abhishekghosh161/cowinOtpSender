package com.abhi.cowinotpsender

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class NotificationCancelReceiver : BroadcastReceiver()  {
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.e("abhi", "cancel broadcast received")

        val intent = Intent(context, ForegroundService::class.java).apply {
            putExtra(ForegroundService.ACTION_KEY, "cancel")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(intent);
        } else {
            context?.startService(intent);
        }
    }
}