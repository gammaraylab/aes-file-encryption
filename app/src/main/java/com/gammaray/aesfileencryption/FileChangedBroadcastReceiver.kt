package com.gammaray.aesfileencryption

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gammaray.aesfileencryption.Activity.MainActivity

class FileChangedBroadcastReceiver(private val path: String, private val onChange: () -> Unit) : BroadcastReceiver() {

    companion object {
        const val EXTRA_PATH = "com.gammaray.aesfileencryption.path"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val filePath = intent?.extras?.getString(EXTRA_PATH).toString()
        if (filePath == path)
            onChange.invoke()
        MainActivity.log("onChange")
    }
}