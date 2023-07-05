package com.example.loadimagefromgallery

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    private var requestCodeData = 101
    private val videoAdapter = VideoAdapter()
    private lateinit var recyclerView: RecyclerView
    private lateinit var videoSP : Spinner
    val nameList : ArrayList<String>  = ArrayList()
    var folderName : ArrayList<VideoUrlData> = ArrayList()
    private var progressDialog: ProgressDialog? = null
    //private lateinit var video : ArrayList<VideoModal>
    val videosList = ArrayList<VideoModal>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initData()
        initClick()

    }

    private fun initData() {
        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_MEDIA_VIDEO) ==
            PackageManager.PERMISSION_GRANTED){
            // video = fetchVideosFromGallery()
            fetchVideosFromGallery()

        }else if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED){
            fetchVideosFromGallery()
            // video = fetchVideosFromGallery()
        }else{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO),requestCodeData)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),requestCodeData)
            }
        }
    }

    private fun initClick() {
        videoSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //Toast.makeText(this@MainActivity, folderName[p2], Toast.LENGTH_SHORT).show()
                if(nameList[p2] == folderName[p2].videoName){
                    if(nameList[p2] == "Gallery"){
                        fetchVideosFromGallery()
                    }else{
                        // video = getVideoDataFromFolder(folderName[p2].videoPath)
                        videosList.clear()
                        getVideoDataFromFolder(folderName[p2].videoPath)
                    }
                    Log.d("videoPath", "onItemSelected: ${folderName.get(p2).videoPath}")
                    Log.d("videoPath", "onItemSelected: ${folderName.get(p2).videoName}")
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycleViewID)
        videoSP = findViewById(R.id.videoSP)

        folderName = getAllVideoFolderNames()

        for(vName in folderName){
            nameList.add(vName.videoName)
        }

        val videoSPAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            nameList
        )

        videoSP.adapter = videoSPAdapter

        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity,4)
            adapter = videoAdapter
        }
    }

    private fun getVideoDataFromFolder(folderPath: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(R.layout.progress)
        val dialog = alertDialogBuilder.create()
        dialog.setCancelable(false)
        dialog.show()

        lifecycleScope.launch(Dispatchers.IO) {

            val folder = File(folderPath)
            val videoFiles = folder.listFiles{_,name -> name.endsWith(".mp4")}
            val retriever = MediaMetadataRetriever()

            videoFiles?.forEach { file ->
                retriever.setDataSource(file.absolutePath)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                val name = file.name
                if(duration!=null){
                    val videoModal = VideoModal(filePath = file.absolutePath,name = name, duration = duration)
                    videosList.add(videoModal)
                }
            }

            withContext(Dispatchers.Main) {
                videoAdapter.loadData(videosList)
                dialog.dismiss()
            }

            retriever.release()

        }


    }

    private fun fetchVideosFromGallery() {
        videosList.clear()


        lifecycleScope.launch(Dispatchers.IO){
            val projection = arrayOf(
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION
            )
            val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
            val cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
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

            withContext(Dispatchers.Main){
                videoAdapter.loadData(videosList)
            }
        }

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
           fetchVideosFromGallery()
            //video = fetchVideosFromGallery()

       }else{
            Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show()
       }

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