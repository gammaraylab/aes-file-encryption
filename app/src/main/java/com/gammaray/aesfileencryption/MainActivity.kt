package com.gammaray.aesfileencryption

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View

class MainActivity : AppCompatActivity(), FileListFragment.OnItemClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility=window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        setContentView(R.layout.activity_main)
        if(savedInstanceState==null){
            val fileListFragment=FileListFragment.build {
                path=Environment.getExternalStorageDirectory().absolutePath
            }
            supportFragmentManager.beginTransaction()
                .add(R.id.container,fileListFragment)
                .addToBackStack(Environment.getExternalStorageDirectory().absolutePath)
                .commit()
        }
    }

    override fun onCLick(fileModel: FileModel) {
        if(fileModel.fileType==FileType.FILE)
            addFileFragment(fileModel)
        else
            launchFileIntent(fileModel)
    }

    override fun onLongClick(fileModel: FileModel) {
        TODO("Not yet implemented")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        }
    }
    private fun addFileFragment(fileModel: FileModel){
        val fileListFragment=FileListFragment.build {
            path=fileModel.path
        }
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fileListFragment)
        fragmentTransaction.addToBackStack(fileModel.path)
        fragmentTransaction.commit()
    }
}