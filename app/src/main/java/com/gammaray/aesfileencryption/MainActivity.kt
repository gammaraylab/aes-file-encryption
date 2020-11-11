package com.gammaray.aesfileencryption

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT_TREE
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.widget.ActivityChooserView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FileListFragment.OnItemClickListener {

    private val backStackManager=BackStackManager()
    private lateinit var breadcrumbsRecyclerAdapter: BreadcrumbsRecyclerAdapter
    private val READ_REQUEST_CODE=453

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.decorView.systemUiVisibility=window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        setContentView(R.layout.activity_main)
        checkPermissions()
        if(savedInstanceState==null){
            val fileListFragment=FileListFragment.build {
                path=/*getExternalFilesDir(null)?.absolutePath.toString()*/Environment.getExternalStorageDirectory().absolutePath
            }
            supportFragmentManager.beginTransaction()
                .add(R.id.container,fileListFragment)
                .addToBackStack(Environment.getExternalStorageDirectory().absolutePath/*getExternalFilesDir(null)?.absolutePath.toString()*/)
                .commit()
        }

//        openDocTree()

        initViews()
        initBackStack()
    }
    override fun onCLick(fileModel: FileModel) {
        if(fileModel.fileType==FileType.FOLDER)
            addFileFragment(fileModel)
        else
            this.launchFileIntent(fileModel)
    }
    override fun onLongClick(fileModel: FileModel) {
        TODO("Not yet implemented")
    }
    override fun onBackPressed() {
        super.onBackPressed()
        backStackManager.popFromStack()
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        }
    }
    private fun addFileFragment(fileModel: FileModel){
        val fileListFragment=FileListFragment.build {
            path=fileModel.path
        }
        backStackManager.addToStack(fileModel)
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fileListFragment)
        fragmentTransaction.addToBackStack(fileModel.path)
        fragmentTransaction.commit()
    }
    private fun initBackStack(){
        backStackManager.onStackChangedListener={
            updateAdapterData(it)
        }
        backStackManager.addToStack(fileModel = FileModel(Environment.getExternalStorageDirectory().absolutePath,FileType.FOLDER,"/",0.0))
    }
    private fun initViews(){
//        setSupportActionBar(toolBar)
        recyclerViewBreadcrumbs.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        breadcrumbsRecyclerAdapter= BreadcrumbsRecyclerAdapter()
        recyclerViewBreadcrumbs.adapter=breadcrumbsRecyclerAdapter
        breadcrumbsRecyclerAdapter.onItemClickListener={
            supportFragmentManager.popBackStack(it.path,2)
            backStackManager.popFromStackTill(it)

        }
    }
    private fun updateAdapterData(files:List<FileModel>){
        breadcrumbsRecyclerAdapter.updateData(files)
        if(files.isNotEmpty()){
            recyclerViewBreadcrumbs.smoothScrollToPosition(files.size-1)
        }
    }
    private fun checkPermissions() {
        val permissionRead =
                ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE")
        val permissionWrite =
                ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE")
        if (permissionRead == 0 && permissionWrite == 0)
            return
        requestPermission()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
                        "android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"
                ), READ_REQUEST_CODE)
    }

    private fun openDocTree(){
        val intent=Intent(ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(intent,READ_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK)
            Log.i("TAG","${data?.data}")

    }

    override fun onRequestPermissionsResult(requestCode: Int, permission: Array<String>,grantResults: IntArray) = when {
        requestCode != READ_REQUEST_CODE -> super.onRequestPermissionsResult(requestCode, permission, grantResults)
        grantResults[0] != 0 -> requestPermission()
        else -> {
            finish()
            startActivity(intent)
        }
    }

}