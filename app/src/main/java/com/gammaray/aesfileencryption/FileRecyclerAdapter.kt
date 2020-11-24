package com.gammaray.aesfileencryption

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycler_file.view.*
import java.lang.StringBuilder

class FilesRecyclerAdapter : RecyclerView.Adapter<FilesRecyclerAdapter.ViewHolder>() {

    var onItemClickListener: ((FileModel) -> Unit)? = null
    var onItemLongClickListener: ((FileModel) -> Unit)? = null

    var filesList = listOf<FileModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_file, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount() = filesList.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindView(position)
    fun updateData(filesList: List<FileModel>) {
        this.filesList = filesList
        notifyDataSetChanged()
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
        override fun onClick(v: View?) {
            onItemClickListener?.invoke(filesList[adapterPosition])
        }
        override fun onLongClick(v: View?): Boolean {
            onItemLongClickListener?.invoke(filesList[adapterPosition])
            return true
        }
        fun bindView(position: Int) {
            val fileModel = filesList[position]
            itemView.nameTextView.text = fileModel.name
            itemView.clipToOutline=true
            val sb=StringBuilder()

            if (fileModel.fileType == FileType.FOLDER) {
                itemView.folderTextView.visibility = View.VISIBLE
                itemView.totalSizeTextView.visibility = View.GONE
                sb.clear()
                sb.append("(")
                sb.append(fileModel.subFiles)
                sb.append(" files)")
                itemView.folderTextView.text = sb
            } else {
                itemView.folderTextView.visibility = View.GONE
                itemView.totalSizeTextView.visibility = View.VISIBLE
                sb.clear()

                if(fileModel.sizeInGB>=1) {
                    sb.append(String.format("%.2f", fileModel.sizeInGB))
                    sb.append(" GB")
                }
                else if(fileModel.sizeInMB>=1) {
                    sb.append(String.format("%.2f", fileModel.sizeInMB))
                    sb.append(" MB")
                }
                else if(fileModel.sizeInKB>=1) {
                    sb.append(String.format("%.2f", fileModel.sizeInKB))
                    sb.append(" KB")
                }
                else {
                    sb.append(fileModel.sizeInB)
                    sb.append(" Byte")
                }

                itemView.totalSizeTextView.text = sb
            }
        }
    }
}