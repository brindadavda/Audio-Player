package com.example.audioplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val actionName = intent.action
        val serviceIntent = Intent(context, MusicService::class.java)

        when (actionName) {
            ApplicationClass.ACTION_PLAY -> {
                serviceIntent.putExtra("ActionName", "playPause")
                context.startService(serviceIntent)
            }

            ApplicationClass.ACTION_NEXT -> {
                serviceIntent.putExtra("ActionName", "next")
                context.startService(serviceIntent)
            }

            ApplicationClass.ACTION_PREVIOUS -> {
                serviceIntent.putExtra("ActionName", "previous")
                context.startService(serviceIntent)
            }
        }
    }
}
