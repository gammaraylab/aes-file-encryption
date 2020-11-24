package com.gammaray.aesfileencryption

data class FileModel(
        val path: String,
        val fileType: FileType,
        val name: String,
        val sizeInB: Long,
        val extension: String = "",
        val subFiles: Int = 0) {
    val sizeInKB:Double=sizeInB.toDouble()/1024
    val sizeInMB=sizeInKB/1024
    val sizeInGB=sizeInMB/1024
    fun onlyPath()=path.replace(name,"")
    fun nameWithoutExtension()=name.replace(".${extension}","")

}