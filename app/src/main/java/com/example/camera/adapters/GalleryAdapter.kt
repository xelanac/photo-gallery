package com.example.camera.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.camera.R
import com.example.camera.interfaces.ClickEventView
import java.io.File

class GalleryAdapter(private val pics : List<File>, context: Context, var handler: ClickEventView):RecyclerView.Adapter<GalleryAdapter.PicViewHolder>(){

    private val contextFromFragment = context
    private var nameFilePic = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pic_item, parent, false)

        return PicViewHolder(view, handler, contextFromFragment)
    }

    override fun onBindViewHolder(holder: PicViewHolder, position: Int) {
        val pic = pics[position]

        holder.setContent(pic)
    }

    override fun getItemCount(): Int { return pics.size }

    class PicViewHolder(picView: View, var handler: ClickEventView, var context: Context): RecyclerView.ViewHolder(picView), View.OnClickListener{
        val picPreview = picView.findViewById<ImageView>(R.id.pic)
        val namePic = picView.findViewById<TextView>(R.id.name_pic)
        val card = picView.findViewById<ImageView>(R.id.forward_arrow)

        var picObject = File("")

        init {
            card.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            handler.clickOnItemInList(picObject.toUri().lastPathSegment.toString())
        }

      fun setContent(pic: File){
          this.picObject = pic

          Glide.with(context).load(pic).into(picPreview)
          namePic.text = pic.toUri().lastPathSegment.toString()
      }

    }


}