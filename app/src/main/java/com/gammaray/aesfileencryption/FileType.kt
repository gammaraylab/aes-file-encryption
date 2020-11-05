package com.gammaray.aesfileencryption

import java.io.File

enum class FileType {
    FILE,
    FOLDER;
    companion object{
        fun fileType(file:File)=when(file.isDirectory){
            true->FOLDER
            false->FILE
        }

    }
}