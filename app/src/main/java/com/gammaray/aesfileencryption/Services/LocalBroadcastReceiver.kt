package com.gammaray.aesfileencryption.Services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LocalBroadcastReceiver : BroadcastReceiver() {
    companion object{
        const val ACTION_FILE_CHANGED="com.gammaray.aesfileencryption.Services.file_changed"

        const val EXTRA_MESSAGE="com.gammaray.aesfileencryption.Services.message"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action== ACTION_FILE_CHANGED){

        }
    }
}