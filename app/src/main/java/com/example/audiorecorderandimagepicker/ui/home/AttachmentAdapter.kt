package com.example.audiorecorderandimagepicker.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.audiorecorderandimagepicker.R
import com.squareup.picasso.Picasso
import java.io.File

class AttachmentAdapter(photoList: List<Attachment>, listener: ImageAdapterListener) :
    RecyclerView.Adapter<AttachmentAdapter.PhotoItem>() {
    var photoList: List<Attachment> = ArrayList()
    var listener: ImageAdapterListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoItem {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_selected_attachment, parent, false)
        return PhotoItem(view)
    }

    override fun onBindViewHolder(holder: PhotoItem, position: Int) {
        holder.photoItem.adjustViewBounds = true
        if (photoList[position].type == AttachmentType.AUDIO) {
            holder.photoItem.setImageResource(R.drawable.ic_play)
        } else {
            Picasso.get().load(File(photoList[position].path)).into(holder.photoItem)
        }
    }

    override fun getItemCount(): Int {
        return photoList.size
    }

    inner class PhotoItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var photoItem: ImageView
        var closeButton: ImageView

        init {
            photoItem = itemView.findViewById(R.id.photoItem)
            closeButton = itemView.findViewById(R.id.closeButton)
            photoItem.setOnClickListener {
                listener.onClickAttachment(
                    adapterPosition
                )
            }
            closeButton.setOnClickListener { listener.onClickRemove(adapterPosition) }
        }
    }

    interface ImageAdapterListener {
        fun onClickRemove(position: Int)
        fun onClickAttachment(position: Int)
    }

    init {
        this.photoList = photoList
        this.listener = listener
    }

    data class Attachment(
        val path: String,
        val type: AttachmentType
    )
}