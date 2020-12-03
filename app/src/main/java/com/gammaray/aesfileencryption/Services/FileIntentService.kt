package com.gammaray.aesfileencryption.Services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.gammaray.aesfileencryption.Activity.MainActivity
import com.gammaray.aesfileencryption.FileChangedBroadcastReceiver
import com.gammaray.aesfileencryption.R
import java.io.*
import java.security.MessageDigest
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class FileIntentService : IntentService("FileIntentService") {

    companion object {
        const val ACTION_COPY: String = "com.gammaray.aesfileencryption.fileservice.copy"
        const val ACTION_CUT:String = "com.gammaray.aesfileencryption.fileservice.cut"
        const val ACTION_ENCRYPT:String = "com.gammaray.aesfileencryption.fileservice.encrypt"
        const val ACTION_DECRYPT:String = "com.gammaray.aesfileencryption.fileservice.decrypt"

        private const val BLOCK_SIZE=1024*512
        private const val ENCRYPT="encrypt"
        const val EXTRA_FILE_SOURCE_PATH: String = "com.gammaray.aesfileencryption.fileservice.source_path"
        const val EXTRA_FILE_DESTINATION_PATH: String = "com.gammaray.aesfileencryption.fileservice.destination_path"
        const val EXTRA_KEY:String="com.gammaray.aesfileencryption.fileservice.key"
    }

    override fun onHandleIntent(intent: Intent?) {
        MainActivity.log("Starting service")
        if (intent?.action == ACTION_COPY) {
            if (intent.hasExtra(EXTRA_FILE_SOURCE_PATH) && intent.hasExtra(EXTRA_FILE_DESTINATION_PATH)) {
                copyFile(
                        intent.getStringExtra(EXTRA_FILE_SOURCE_PATH).toString(),
                        intent.getStringExtra(EXTRA_FILE_DESTINATION_PATH).toString()
                )
            }
        }
        else if(intent?.action == ACTION_ENCRYPT)
            encrypt(intent.getStringExtra(EXTRA_KEY).toString(),File(intent.getStringExtra(EXTRA_FILE_SOURCE_PATH).toString()))

        else if(intent?.action== ACTION_DECRYPT)
            decrypt(intent.getStringExtra(EXTRA_KEY).toString(),File(intent.getStringExtra(EXTRA_FILE_SOURCE_PATH).toString()))

    }
    private fun copyFile(source: String, destination: String) {
        val sourceFile = File(source)
        var destinationFile = File(destination , sourceFile.name)

        var counter = 2
        while (destinationFile.exists()) {
            destinationFile = File(destination , "${sourceFile.nameWithoutExtension}-copy (${counter++}).${sourceFile.extension}")
        }

        sourceFile.copyTo(destinationFile)
        val broadcastIntent = Intent()
        broadcastIntent.action = applicationContext.getString(R.string.file_change_broadcast)
        broadcastIntent.putExtra(FileChangedBroadcastReceiver.EXTRA_PATH, destination)
        sendBroadcast(broadcastIntent)
    }
    private fun encrypt(key:String, source:File){
        var secretKeySpec:SecretKeySpec?=null
        var output:File?=null
        try{
            val sha: MessageDigest = MessageDigest.getInstance("SHA-1")
            var tmp=sha.digest(key.toByteArray())
            tmp= tmp.copyOf(16)
            secretKeySpec=SecretKeySpec(tmp, "AES")
        }catch (e: Exception){
            e.printStackTrace()
        }

        try {
            val iv=ByteArray(16)
            Random().nextBytes(iv)
            val ivSpec= IvParameterSpec(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)

            var tmp=File(source.parent, "${source.name}.$ENCRYPT")
            var count=1
            while (tmp.exists())
                tmp = File(source.parent , "${source.nameWithoutExtension}(${count++}).${source.extension}.$ENCRYPT")
            output=tmp
            val fileOs= FileOutputStream(output)
            val fileIs= FileInputStream(source)
            val cipherOS= CipherOutputStream(fileOs,cipher)
            fileOs.write(iv)

            val buffer = ByteArray(BLOCK_SIZE)

            while (fileIs.read(buffer).also { count = it } > 0)
                cipherOS.write(buffer, 0, count)

            cipherOS.flush()
            cipherOS.close()
            fileOs.close()
            fileIs.close()

            output.createNewFile()
        }catch (e: IOException){
            e.printStackTrace()
            if(output?.exists() as Boolean)
                output.delete()
        }catch (e: java.lang.Exception){
            e.printStackTrace()
            if(output?.exists() as Boolean)
                output.delete()
        }
    }
    private fun decrypt(key:String, source:File){
        var secretKeySpec:SecretKeySpec?=null
        var output:File?=null
        try{
            val sha: MessageDigest = MessageDigest.getInstance("SHA-1")
            var tmp=sha.digest(key.toByteArray())
            tmp= tmp.copyOf(16)
            secretKeySpec=SecretKeySpec(tmp, "AES")
        }catch (e: Exception){
            e.printStackTrace()
        }
        try{
            val iv=ByteArray(16)
            val fileIs=FileInputStream(source)
            fileIs.read(iv,0,16)    //reading iv from the encrypted file i.e iv is stored in first 16 bytes of encrypted file
            fileIs.close()

            val ivSpec=IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)
            var tmp=File(source.parent,source.nameWithoutExtension)
            tmp=File(source.parent, "${tmp.nameWithoutExtension}-decrypted.${tmp.extension}")
            var count=1
            while (tmp.exists())
                tmp=File(source.parent, "${tmp.nameWithoutExtension}-decrypted(${count++}).${tmp.extension}")
            output=tmp
            val fileOs=FileOutputStream(output)
            val cipherOS=CipherOutputStream(fileOs,cipher)
            val buffer = ByteArray(BLOCK_SIZE)
            val raf= RandomAccessFile(source,"r")
            raf.seek(16)

            while (raf.read(buffer).also { count = it } > 0)
                cipherOS.write(buffer, 0, count)

            cipherOS.flush()
            cipherOS.close()
            fileOs.close()
            raf.close()

            output.createNewFile()
        }catch (e: BadPaddingException){
            e.printStackTrace()
            MainActivity.toast("wrong key or File tampered")
            if(output?.exists() as Boolean)
                output.delete()
        }catch (e: IllegalBlockSizeException){
            e.printStackTrace()
            MainActivity.toast("file tampered")
            if(output?.exists() as Boolean)
                output.delete()
        }catch (e: IOException){
            e.printStackTrace()
            if(output?.exists() as Boolean)
                output.delete()
        }catch (e: Exception){
            e.printStackTrace()
            if(output?.exists() as Boolean)
                output.delete()
        }
    }

}