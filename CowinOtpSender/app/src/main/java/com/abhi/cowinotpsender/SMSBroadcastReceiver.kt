package com.abhi.cowinotpsender

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import com.abhi.cowinotpsender.ForegroundService.Companion.ACTION_KEY
import com.abhi.cowinotpsender.ForegroundService.Companion.MSG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SMSBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if("android.provider.Telephony.SMS_RECEIVED" == intent.action)
        {
                intent.extras.let {
                    val sms = it?.get("pdus") as Array<*>
                    for (i in sms.indices) {
                        val smsMessage: SmsMessage =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val format = it.getString("format")
                                SmsMessage.createFromPdu(sms[i] as ByteArray, format)
                            } else {
                                SmsMessage.createFromPdu(sms[i] as ByteArray)
                            }
                        val sender = smsMessage.originatingAddress.toString()
                        val messageBody = smsMessage.messageBody.toString()

                        if (messageBody.contains("CoWIN")) {
                            sendCoWinSms(context, messageBody)
                        }
                    }
                }
            Log.e("abhi", "On SMS received")
        }
    }

    private fun sendCoWinSms(context: Context?, sms: String)
    {
        val intent = Intent(context, ForegroundService::class.java).apply {
            putExtra(MSG, sms)
            putExtra(ACTION_KEY, "sms")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(intent);
        } else {
            context?.startService(intent);
        }

    }
}