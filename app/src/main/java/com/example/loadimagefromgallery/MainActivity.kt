package com.example.loadimagefromgallery

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private var requestCodeData = 101
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayout: LinearLayout
    private lateinit var video : ArrayList<VideoModal>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycleViewID)
        linearLayout = findViewById(R.id.video_gallery)

        linearLayout.setOnClickListener {
            Toast.makeText(this, "video_gallery", Toast.LENGTH_SHORT).show()
           // getAllGalleryFolderNames(this)
        }

        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED){
            video = fetchVideosFromGallery()

        }else{

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),requestCodeData)
        }

        val videoAdapter = VideoAdapter(video)
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity,4)
            adapter = videoAdapter
        }


    }

    private fun fetchVideosFromGallery() : ArrayList<VideoModal>{
        val videosList = ArrayList<VideoModal>()

        val projection = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION
        )

        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val filePathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            while (it.moveToNext()){
                val filePath = it.getString(filePathColumn)
                val name = it.getString(nameColumn)
                val duration = it.getLong(durationColumn)

                val video = VideoModal(filePath,name,duration)
                videosList.add(video)
            }

        }
        return videosList
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == requestCodeData && grantResults.isNotEmpty() &&
           grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show()
            video = fetchVideosFromGallery()

       }else{
            Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show()
       }

      //  return

    }

    /*fun getAllGalleryFolderNames(context: Context)  {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC"
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = context.getContentResolver().query(
            uri,
            projection,
            null,
            null,
            sortOrder
        )
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    val filePath: String =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                    val folderName = getFolderNameFromPath(filePath)
                    // Do something with the folder name, such as displaying it or storing it in a list for further use.
                    println("Folder name: $folderName")
                }
            } finally {
                cursor.close()
            }
        }
    }

    private fun getFolderNameFromPath(filePath: String): String {
        val lastSeparatorIndex = filePath.lastIndexOf('/')
        return if (lastSeparatorIndex != -1) {
            filePath.substring(0, lastSeparatorIndex)
        } else ""
    }*/

}