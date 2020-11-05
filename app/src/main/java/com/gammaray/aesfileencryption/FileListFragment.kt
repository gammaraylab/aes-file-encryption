package com.gammaray.aesfileencryption

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_file_list.*
import java.lang.Exception

class FileListFragment : Fragment() {
    private lateinit var filesAdapter:FilesRecyclerAdapter
    private lateinit var path:String
    private lateinit var callBack:OnItemClickListener
    interface OnItemClickListener{
        fun onCLick(fileModel:FileModel)
        fun onLongClick(fileModel: FileModel)
    }
    companion object{
        private const val ARG_PATH="com.gammaray.aesfileencryption.fileslist.path"
        fun build(block:Builder.()->Unit)=Builder().apply(block).build()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_list,container,false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            callBack=context as OnItemClickListener
        }catch (e: Exception){
            throw Exception("${context} should implement FileListFragment.OnItemClickListener")
        }
    }
    class Builder{
        var path=""

        fun build():FileListFragment{
            val fragment=FileListFragment()
            val args=Bundle()
            args.putString(ARG_PATH,path)
            fragment.arguments=args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filePath = arguments?.getString(ARG_PATH)
        if (filePath == null) {
            Toast.makeText(context, "Path should not be null!", Toast.LENGTH_SHORT).show()
            return
        }
        path = filePath

        initViews()
    }

    private fun initViews() {
        filesRecyclerView.layoutManager = LinearLayoutManager(context)
        filesAdapter = FilesRecyclerAdapter()
        filesRecyclerView.adapter = filesAdapter
        updateDate()
    }

    fun updateDate() {
        val list= getFilesFromPath(path) ?: return
        val files =fileModelsFromFiles(list)
        if (files.isEmpty()) {
            emptyFolderLayout.visibility = View.VISIBLE
        } else {
            emptyFolderLayout.visibility = View.INVISIBLE
        }

        filesAdapter.updateData(files)
    }
}