package com.example.loadimagefromgallery

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
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
    private lateinit var videoSP : Spinner
    private lateinit var video : ArrayList<VideoModal>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycleViewID)
        linearLayout = findViewById(R.id.video_gallery)
        videoSP = findViewById(R.id.videoSP)

        val folderName = getAllVideoFolderNames()
        val nameList : ArrayList<String>  = ArrayList()
        for(vName in folderName){
            nameList.add(vName.videoName)
        }

        val videoSPAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            nameList

        )

        videoSP.adapter = videoSPAdapter
        videoSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //Toast.makeText(this@MainActivity, folderName[p2], Toast.LENGTH_SHORT).show()
                Log.d("videoPath", "onItemSelected: ${folderName.get(p2).videoPath}")
                Log.d("videoPath", "onItemSelected: ${folderName.get(p2).videoName}")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }



        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_MEDIA_VIDEO) ==
            PackageManager.PERMISSION_GRANTED){
            video = fetchVideosFromGallery()

        }else if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED){
            video = fetchVideosFromGallery()
        }else{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO),requestCodeData)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),requestCodeData)
            }
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
             //   Log.e("filePath", "fetchVideosFromGallery: filePath : $filePath" )
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


    private fun getAllVideoFolderNames() : ArrayList<VideoUrlData>{
        val folderNames = HashSet<VideoUrlData>()
        val videoListArray = ArrayList<VideoUrlData>()
        val contentResolver : ContentResolver = contentResolver
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val selection = "${MediaStore.Video.Media.DATA} like ?"
        val selectionArgs = arrayOf("%.mp4")
        val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

        val cursor : Cursor? = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        // Iterate over the cursor and extract folder names
        folderNames.clear()
       // videoListArray.clear()
        cursor?.use {
            while (cursor.moveToNext()){
                val videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val folderPath = videoPath.substring(0,videoPath.lastIndexOf("/"))
                //val folderName = videoPath.substringBeforeLast("/")
                val folderNameWithoutPath = folderPath.substringAfterLast("/")
                folderNames.add(VideoUrlData(folderPath, folderNameWithoutPath))
            }
        }
        videoListArray.add(0,VideoUrlData("", "Gallery"))
        videoListArray.addAll(folderNames)
        return videoListArray

    }

}