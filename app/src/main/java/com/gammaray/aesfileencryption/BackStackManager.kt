package com.gammaray.aesfileencryption

class BackStackManager {
    private var files= mutableListOf<FileModel>()
    var onStackChangedListener:((List<FileModel>)->Unit)?=null
    val top:FileModel
        get() = files.last()
    fun addToStack(fileModel: FileModel){
        files.add(fileModel)
        onStackChangedListener?.invoke(files)
    }
    fun popFromStack(){
        if(files.isNotEmpty()){
            files.removeLast()
            onStackChangedListener?.invoke(files)
        }
    }
    fun popFromStackTill(fileModel: FileModel){
        files=files.subList(0,files.indexOf(fileModel)+1)
        onStackChangedListener?.invoke(files)
    }
}