package com.gammaray.aesfileencryption

import android.app.IntentService
import android.content.Intent
import android.util.Log
import java.io.File

class FileIntentService : IntentService("FileIntentService") {

    companion object {
        const val ACTION_COPY: String = "com.gammaray.aesfileencryption.fileservice.copy"

        const val EXTRA_FILE_SOURCE_PATH: String = "com.gammaray.aesfileencryption.fileservice.source_path"
        const val EXTRA_FILE_DESTINATION_PATH: String = "com.gammaray.aesfileencryption.fileservice.destination_path"
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d("TAG", "Starting service")
        if (intent?.action == ACTION_COPY) {
            if (intent.hasExtra(EXTRA_FILE_SOURCE_PATH) && intent.hasExtra(EXTRA_FILE_DESTINATION_PATH)) {
                copyFile(
                        intent.getStringExtra(EXTRA_FILE_SOURCE_PATH).toString(),
                        intent.getStringExtra(EXTRA_FILE_DESTINATION_PATH).toString()
                )
            }
        }
    }

    private fun copyFile(source: String, destination: String) {
        val sourceFile = File(source)
        var destinationFile = File(destination + "/${sourceFile.nameWithoutExtension}-copy.${sourceFile.extension}")

        var counter = 2
        while (destinationFile.exists()) {
            destinationFile = File(destination + "/${sourceFile.nameWithoutExtension}-copy ($counter).${sourceFile.extension}")
        }

        sourceFile.copyTo(destinationFile)

        val broadcastIntent = Intent()
        broadcastIntent.action = applicationContext.getString(R.string.file_change_broadcast)
        broadcastIntent.putExtra(FileChangedBroadcastReceiver.EXTRA_PATH, destination)
        sendBroadcast(broadcastIntent)
    }
}