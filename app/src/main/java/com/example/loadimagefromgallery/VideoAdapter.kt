package com.example.loadimagefromgallery

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VideoAdapter(private val videos : List<VideoModal>) :
    RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = videos[position]
        val long_to_milli = video.duration / 1000
        val minute = (long_to_milli % 3600) / 60
        val second = long_to_milli % 60
        val timeString = String.format("%02d:%02d",minute,second)
        holder.videoDuration.text = timeString

        Glide.with(holder.videoThumbnail.context)
            .load(video.filePath)
            .into(holder.videoThumbnail)

        holder.videoThumbnail.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(holder.videoDuration.context)
            val customView : View = LayoutInflater.from(holder.videoDuration.context).inflate(R.layout.video_dialog,null)
            alertDialogBuilder.setView(customView)

            val dialog = alertDialogBuilder.create()
            val videoMessage : TextView = customView.findViewById(R.id.video_message)
            val videoTitle : TextView = customView.findViewById(R.id.video_title)
            val cancelBtn : ImageView = customView.findViewById(R.id.video_upload_cancel)

            if(second <= 30 && minute < 1){
                dialog.show()
                videoTitle.text = "Video uploded!!"
                cancelBtn.setOnClickListener {
                    dialog.dismiss()
                }
            }else{
                dialog.show()
                videoTitle.text = "Video can not be uploded!!"
                videoMessage.text = "This video is more than 30 sec. Chose another video to upload."
                cancelBtn.setOnClickListener {
                    dialog.dismiss()
                }


            }
        }
    }

    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val videoThumbnail: ImageView = view.findViewById(R.id.video_thumbnail)
        val videoDuration: TextView = view.findViewById(R.id.video_duration)
    }
}