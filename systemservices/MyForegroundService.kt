package com.system.systemservices

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import android.graphics.PixelFormat
import android.view.LayoutInflater
import android.view.WindowManager.LayoutParams.TYPE_PHONE

class MyForegroundService : Service() {

    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()

        // Set up an invisible overlay to suppress indicators (stealth mode)
        setupOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the overlay when the service is destroyed
        overlayView?.let {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)
            overlayView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun setupOverlay() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            1, // Width
            1, // Height
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        overlayView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null)
        overlayView?.visibility = View.INVISIBLE

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}