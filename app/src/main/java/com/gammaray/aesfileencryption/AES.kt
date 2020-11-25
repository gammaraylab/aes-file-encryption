package com.gammaray.aesfileencryption

import android.util.Log
import java.io.File
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.AlgorithmConstraints
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES(key: String) {
    private lateinit var secretKeySpec: SecretKeySpec

    init {
        var tmp=key.toByteArray(Charsets.UTF_8)
        try{
            val sha:MessageDigest= MessageDigest.getInstance("SHA-1")
            tmp=sha.digest(tmp)
            tmp= tmp.copyOf(16)
            secretKeySpec=SecretKeySpec(tmp,"AES")
        }catch (e:NoSuchAlgorithmException){
            e.printStackTrace()
        }catch (e:UnsupportedEncodingException){
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun encrypt(fileModel: FileModel):File?{
        val file = File(fileModel.path)
        try {
            val tmp = file.readBytes()
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
//            val iv=ByteArray(16)
//            Random().nextBytes(iv)
            val iv = "my name is anadi".toByteArray()
            val ivSpec=IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec,ivSpec)

            val output=File(fileModel.onlyPath(),"${fileModel.nameWithoutExtension()}-encrypted.${fileModel.extension}")

            output.writeBytes(cipher.doFinal(tmp))
            Log.e("enc",output.readText())

            return output
        }catch (e: BadPaddingException){
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
        return null
    }

    fun decrypt(fileModel: FileModel):File?{
        val file = File(fileModel.path)
        try {
            val tmp = file.readBytes()
            Log.e("dec",file.readText())
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv:ByteArray="my name is anadi".toByteArray()
            val ivSpec=IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec,ivSpec)

            val output=File(fileModel.onlyPath(),"${fileModel.nameWithoutExtension()}-decrypted.${fileModel.extension}")
            output.writeBytes(cipher.doFinal(tmp))

            return output
        }catch (e: BadPaddingException){
            e.printStackTrace()
        }catch (e: IllegalBlockSizeException){
            e.printStackTrace()
            MainActivity.errorDisplay(e.message.toString().plus("\nFile may be tampered"))
        }catch (e:Exception){
            e.printStackTrace()
        }

        return null
    }

}