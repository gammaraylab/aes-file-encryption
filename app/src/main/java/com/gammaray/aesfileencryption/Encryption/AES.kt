package com.gammaray.aesfileencryption.Encryption

import com.gammaray.aesfileencryption.Activity.MainActivity
import com.gammaray.aesfileencryption.FileUtils.FileModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES(key: String) {
    private val blockSize= 1024*512
    private lateinit var secretKeySpec: SecretKeySpec

    companion object{
        val ENCRYPT="encrypt"
    }


    init {
        try{
            val sha:MessageDigest= MessageDigest.getInstance("SHA-1")
            var tmp=sha.digest(key.toByteArray())
            tmp= tmp.copyOf(16)
            secretKeySpec=SecretKeySpec(tmp, "AES")
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

            val output:File? = File(fileModel.onlyPath(), "${fileModel.name}.$ENCRYPT")
            val fileOs=FileOutputStream(output)
            val fileIs=FileInputStream(File(fileModel.path))
            val cipherOS=CipherOutputStream(fileOs,cipher)
            fileOs.write(iv)

            val buffer = ByteArray(blockSize)
            var count:Int

            while (fileIs.read(buffer).also { count = it } > 0)
                cipherOS.write(buffer, 0, count)

            cipherOS.flush()
            cipherOS.close()
            fileOs.close()
            fileIs.close()

            return output
        }catch (e: Exception){
            e.printStackTrace()
            MainActivity.toast("Cannot encrypt")
        }
        return null
    }

    fun decrypt(fileModel: FileModel):File?{
        try {
            val source=File(fileModel.path)
            val iv=ByteArray(16)
            val fileIs=FileInputStream(source)
            fileIs.read(iv,0,16)    //reading iv from the encrypted file i.e iv is stored in first 16 bytes of encrypted file
            fileIs.close()

            val ivSpec=IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)
            MainActivity.log(fileModel.nameWithoutExtension())
            val output=File(fileModel.onlyPath(), "decrypted-${fileModel.nameWithoutExtension()}")
            val fileOs=FileOutputStream(output)
            val cipherOS=CipherOutputStream(fileOs,cipher)
            val buffer = ByteArray(blockSize)
            var count:Int
            val raf= RandomAccessFile(source,"r")
            raf.seek(16)

            while (raf.read(buffer).also { count = it } > 0)
                cipherOS.write(buffer, 0, count)

            cipherOS.flush()
            cipherOS.close()
            fileOs.close()
            raf.close()

            return output
        }catch (e: BadPaddingException){
            e.printStackTrace()
            MainActivity.toast("wrong key or File tampered")
        }catch (e: IllegalBlockSizeException){
            e.printStackTrace()
            MainActivity.toast("file tampered")
        }catch (e: Exception){
            e.printStackTrace()
        }

        return null
    }
}