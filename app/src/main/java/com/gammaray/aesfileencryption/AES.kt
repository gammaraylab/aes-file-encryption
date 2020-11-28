package com.gammaray.aesfileencryption

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES(key: String) {
    private lateinit var secretKeySpec: SecretKeySpec
    private val ENCRYPT="encrypt"
    private val DECRYPT="decrypt"
    private val blockSize= DEFAULT_BUFFER_SIZE

    init {

        try{
//            val sha:MessageDigest= MessageDigest.getInstance("SHA-1")
//            tmp=sha.digest(tmp)
            val tmp="my name is"//(key+CharArray(16-key.length){'*'}).toByteArray()
            val cc=(tmp+String(CharArray(16-tmp.length){'*'})).toByteArray()
//            tmp= tmp.copyOf(16)
            Log.e("TMPKEY",String(cc))
            secretKeySpec=SecretKeySpec(cc, "AES")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun encrypt(fileModel: FileModel):File?{
        try {
            val iv=ByteArray(16)
            Random().nextBytes(iv)
            val ivSpec=IvParameterSpec(iv)


            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)

            val output=aesBufferedOperation(fileModel,cipher,ENCRYPT)
            val ivFile=File(fileModel.onlyPath(), ".${output?.name}.iv")
            ivFile.writeBytes(iv)
            ivFile.createNewFile()
            return output

        }catch (e: BadPaddingException){
            e.printStackTrace()
        }catch (e: Exception){
            e.printStackTrace()
        }
        return null
    }

    fun decrypt(fileModel: FileModel):File?{
        try {
            val ivFile=File(fileModel.onlyPath(), ".${fileModel.name}.iv")
            val iv=ivFile.readBytes()
            val ivSpec=IvParameterSpec(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)

            return aesBufferedOperation(fileModel,cipher,DECRYPT)
        }catch (e: BadPaddingException){
            e.printStackTrace()
        }catch (e: IllegalBlockSizeException){
            e.printStackTrace()
            MainActivity.errorDisplay(e.message.toString().plus("\nFile may be tampered"))
        }catch (e: Exception){
            e.printStackTrace()
        }

        return null
    }

    private fun aesBufferedOperation(fileModel: FileModel, cipher: Cipher,mode:String):File?{
        val output = if(mode==ENCRYPT)
            File(fileModel.onlyPath(), "${fileModel.name}.$mode")
        else{
            File(fileModel.onlyPath(), "decrypted-${fileModel.nameWithoutExtension()}")
        }

        val fileOs=FileOutputStream(output)
        val fileIS=FileInputStream(File(fileModel.path))
        val buffer = ByteArray(blockSize)
        var count:Int
        while (fileIS.read(buffer).also { count = it } > 0){
            fileOs.write(cipher.update(buffer), 0, count)
            Log.e("aesBuffered","$count")
        }
        Log.e("aesBuffered",String(buffer))
        cipher.doFinal(buffer)
        return output
    }

}