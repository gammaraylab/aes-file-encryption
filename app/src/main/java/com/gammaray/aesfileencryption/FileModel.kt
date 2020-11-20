package com.gammaray.aesfileencryption

data class FileModel(
        val path: String,
        val fileType: FileType,
        val name: String,
        val sizeInMB: Double,
        val extension: String = "",
        val subFiles: Int = 0) {
    fun onlyPath()=path.replace(name,"")
    fun nameWithoutExtension()=name.replace(".${extension}","")
    val sizeInB=0
    val sizeInGB=0
    val sizeInKB=0
}