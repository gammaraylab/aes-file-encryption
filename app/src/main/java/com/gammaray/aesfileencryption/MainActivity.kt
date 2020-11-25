package com.gammaray.aesfileencryption

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT_TREE
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alert_dialog_enter_key.view.*
import kotlinx.android.synthetic.main.dialog_enter_name.view.*
import java.io.File
import com.gammaray.aesfileencryption.deleteFile as FileUtilsDeleteFile
class MainActivity : AppCompatActivity(), FileListFragment.OnItemClickListener {

    private val backStackManager=BackStackManager()
    private lateinit var breadcrumbsRecyclerAdapter: BreadcrumbsRecyclerAdapter
    private val READ_REQUEST_CODE=453

    companion object {
        private lateinit var instance:Context
        private const val OPTIONS_DIALOG_TAG: String = "com.gammaray.aesfileencryption.options_dialog"
        fun errorDisplay(error:String){
            Toast.makeText(instance,error,Toast.LENGTH_LONG).show()
        }
    }
    init{
        instance=this
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(savedInstanceState==null){
            val fileListFragment=FileListFragment.build {
                path=Environment.getExternalStorageDirectory().absolutePath
            }
            supportFragmentManager.beginTransaction()
                .add(R.id.container,fileListFragment)
                .addToBackStack(Environment.getExternalStorageDirectory().absolutePath/*getExternalFilesDir(null)?.absolutePath.toString()*/)
                .commit()
        }

        checkPermissions()
        initViews()
        initBackStack()
    }
    override fun onClick(fileModel: FileModel) {
        if(fileModel.fileType==FileType.FOLDER)
            addFileFragment(fileModel)
        else
            launchFileIntent(fileModel)
    }
    override fun onLongClick(fileModel: FileModel) {
        val optionsDialog = FileOptionsDialog.build {}

        optionsDialog.onDeleteClickListener = {
            FileUtilsDeleteFile(fileModel.path)
            updateContentOfCurrentFragment()
        }
        optionsDialog.onCopyClickListener={
            Toast.makeText(this@MainActivity,"not yet implemented",Toast.LENGTH_SHORT).show()
        }
        optionsDialog.onEncryptClickListener={
            var key=""
            val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_enter_key, null)
            val builder=AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Encrypt")
            val alertDialog=builder.show()
            dialogView.proceedButton.setOnClickListener{
                alertDialog.dismiss()
                key=dialogView.keyEditText.toString()
                if(key.isEmpty())
                    Toast.makeText(this,"empty key not allowed",Toast.LENGTH_SHORT).show()
                else if(fileModel.fileType== FileType.FILE ) {
                    val aes = AES(key)
                    val output=aes.encrypt(fileModel)
                    output?.createNewFile()
                    updateContentOfCurrentFragment()
                    }
                else
                    Toast.makeText(this, "cannot encrypt", Toast.LENGTH_SHORT).show()
                }
            dialogView.cancelButton.setOnClickListener{
                alertDialog.dismiss()
            }
        }
        optionsDialog.onDecryptClickListener={
            var key=""
            val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_enter_key, null)
            val builder=AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Decrypt")
            val alertDialog=builder.show()
            dialogView.proceedButton.setOnClickListener{
                alertDialog.dismiss()
                key=dialogView.keyEditText.toString()
                if(key.isEmpty())
                    Toast.makeText(this,"empty key not allowed",Toast.LENGTH_SHORT).show()
                else if(fileModel.fileType== FileType.FILE ) {
                    val aes = AES(key)
                    val output=aes.decrypt(fileModel)
                    output?.createNewFile()
                    updateContentOfCurrentFragment()
                    } else
                        Toast.makeText(this, "cannot decrypt", Toast.LENGTH_SHORT).show()
            }
            dialogView.cancelButton.setOnClickListener{
                alertDialog.dismiss()
            }
        }
        optionsDialog.show(supportFragmentManager, OPTIONS_DIALOG_TAG)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backStackManager.popFromStack()
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuNewFile -> createNewFileInCurrentDirectory()
            R.id.menuNewFolder -> createNewFolderInCurrentDirectory()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun createNewFileInCurrentDirectory() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_enter_name, null)
        view.createButton.setOnClickListener {
            val fileName = view.nameEditText.text.toString()
            if (fileName.isNotEmpty()) {
                createNewFile(fileName, backStackManager.top.path) { _, _ ->
                    bottomSheetDialog.dismiss()
                    updateContentOfCurrentFragment()
                }
            }
        }
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }
    private fun updateContentOfCurrentFragment() {
        val broadcastIntent = Intent()
        broadcastIntent.action = applicationContext.getString(R.string.file_change_broadcast)
        broadcastIntent.putExtra(FileChangedBroadcastReceiver.EXTRA_PATH, backStackManager.top.path)
        sendBroadcast(broadcastIntent)
        onContentChanged()
    }
    private fun createNewFolderInCurrentDirectory() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_enter_name, null)
        view.createButton.setOnClickListener {
            val fileName = view.nameEditText.text.toString()
            if (fileName.isNotEmpty()) {
                createNewFolder(fileName, backStackManager.top.path) { _, _ ->
                    bottomSheetDialog.dismiss()
//                    coordinatorLayout.createShortSnackbar(message)
                    updateContentOfCurrentFragment()
                }
            }
        }
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
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
        backStackManager.onStackChangeListener={
            updateAdapterData(it)
        }
        backStackManager.addToStack(fileModel = FileModel(Environment.getExternalStorageDirectory().absolutePath,FileType.FOLDER,"Internal storage",0))
    }
    private fun initViews(){
        recyclerViewBreadcrumbs.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        breadcrumbsRecyclerAdapter = BreadcrumbsRecyclerAdapter()
        recyclerViewBreadcrumbs.adapter = breadcrumbsRecyclerAdapter
        breadcrumbsRecyclerAdapter.onItemClickListener = {
            supportFragmentManager.popBackStack(it.path, 2);
            backStackManager.popFromStackTill(it)
        }
    }
    private fun updateAdapterData(files:List<FileModel>){
        breadcrumbsRecyclerAdapter.updateData(files)
        if(files.isNotEmpty())
            recyclerViewBreadcrumbs.smoothScrollToPosition(files.size-1)
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