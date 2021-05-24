package com.abhi.cowinotpsender

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceFragmentCompat
import com.abhi.cowinotpsender.ForegroundService.Companion.ACTION_KEY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        geSmsPermission()
        createPromptForDisablingBatteryOptimization()
        startOtpSenderService()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    private fun geSmsPermission() {
        // check if receive sms permission exists
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            // request permission for receiving sms
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), REQUEST_RECEIVE_SMS)
        }
    }

    @SuppressLint("BatteryLife")
    private fun createPromptForDisablingBatteryOptimization()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()

            // get power manager
            val pm = getSystemService(POWER_SERVICE) as PowerManager

            // check if the system is ignoring battery optimization
            if (!pm.isIgnoringBatteryOptimizations(packageName))
            {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun startOtpSenderService() {
        Intent(application, ForegroundService::class.java).apply {
            putExtra(ACTION_KEY, "start")
            startService(this)
        }
    }

    companion object {
        private const val REQUEST_RECEIVE_SMS = 2
    }
}