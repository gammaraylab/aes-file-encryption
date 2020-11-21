package com.gammaray.aesfileencryption

import android.util.Log
import java.io.File
import java.io.UnsupportedEncodingException
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

    fun encrypt(file:File):ByteArray?{
        try {
            val tmp = file.readBytes()
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv=ByteArray(16)
            Random().nextBytes(iv)
            val ivSpec=IvParameterSpec(iv)
            Log.e("ENCRYPT",String(iv))
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec,ivSpec)
            return cipher.doFinal(tmp)+iv
        }catch (e: BadPaddingException){
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
        return null
    }

    fun decrypt(file:File):ByteArray?{
        try {
            val tmp = file.readBytes()
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv=tmp.copyOfRange(tmp.size-16,tmp.size)

            val ivSpec=IvParameterSpec(iv)
            Log.e("DECRYPT",String(iv))
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec,ivSpec)
            return cipher.doFinal(tmp.copyOfRange(0,tmp.size-16))
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