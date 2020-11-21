package com.gammaray.aesfileencryption

import java.io.File
import java.io.UnsupportedEncodingException
import java.security.AlgorithmConstraints
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AES(key: String) {
    private lateinit var secretKey: SecretKeySpec

    init {
        var tmp=key.toByteArray(Charsets.UTF_8)
        try{
            val sha:MessageDigest= MessageDigest.getInstance("SHA-1")
            tmp=sha.digest(tmp)
            tmp= tmp.copyOf(16)
            secretKey=SecretKeySpec(tmp,"AES")
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
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            return cipher.doFinal(tmp)
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
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            return cipher.doFinal(tmp)
        }catch (e: BadPaddingException){
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }

        return null
    }

}