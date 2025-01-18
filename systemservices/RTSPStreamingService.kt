package com.system.systemservices

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import java.net.ServerSocket
import java.io.IOException

class RTSPStreamingService : Service() {

    private lateinit var player: ExoPlayer
    private lateinit var secretKey: SecretKey

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        secretKey = generateEncryptionKey()
    }

    @OptIn(UnstableApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val port = allocatePort()
        val rtspUrl = "rtsp://0.0.0.0:$port/stream"
        val encryptedStreamURL = encryptStreamURL(rtspUrl)

        Log.i("RTSPStreamingService", "Encrypted Stream URL: $encryptedStreamURL")

        val mediaItem = MediaItem.fromUri(rtspUrl)
        val mediaSource = RtspMediaSource.Factory().createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun allocatePort(): Int {
        return try {
            ServerSocket(0).use { it.localPort }
        } catch (e: IOException) {
            Log.e("RTSPStreamingService", "Error allocating port", e)
            1935
        }
    }

    private fun generateEncryptionKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(128)
        return keyGen.generateKey()
    }

    private fun encryptStreamURL(url: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(url.toByteArray(Charsets.UTF_8))
        return android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT)
    }
}