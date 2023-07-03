package com.example.loadimagefromgallery

data class VideoModal(
    val filePath : String,
    val name : String,
    val duration : Long = 0
)

data class VideoUrlData(
    val videoPath : String,
    val videoName : String,
)