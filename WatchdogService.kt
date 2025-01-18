package com.system.systemservices

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.app.ActivityManager

class WatchdogService : Service() {

    private val handler = Handler()
    private val checkInterval: Long = 10000 // Check every 10 seconds

    override fun onCreate() {
        super.onCreate()
        Log.i("WatchdogService", "WatchdogService started")
        scheduleServiceCheck()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scheduleServiceCheck()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        Log.i("WatchdogService", "WatchdogService stopped")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun scheduleServiceCheck() {
        handler.postDelayed({
            checkServices()
            scheduleServiceCheck()
        }, checkInterval)
    }

    private fun checkServices() {
        Log.i("WatchdogService", "Checking critical services")

        // Check MyForegroundService
        if (!isServiceRunning(MyForegroundService::class.java)) {
            Log.w("WatchdogService", "MyForegroundService not running, restarting...")
            startService(Intent(this, MyForegroundService::class.java))
        }

        // Check RTSPStreamingService
        if (!isServiceRunning(RTSPStreamingService::class.java)) {
            Log.w("WatchdogService", "RTSPStreamingService not running, restarting...")
            startService(Intent(this, RTSPStreamingService::class.java))
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}