package com.gammaray.aesfileencryption

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FileListFragment.OnItemClickListener {

    private val backStackManager=BackStackManager()
    private lateinit var breadcrumbsRecyclerAdapter: BreadcrumbsRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.decorView.systemUiVisibility=window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
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
        initViews()
        initBackStack()
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
        setSupportActionBar(toolBar)
//        recyclerViewBreadcrumbs.layoutManager=LinearLayoutManager
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
}