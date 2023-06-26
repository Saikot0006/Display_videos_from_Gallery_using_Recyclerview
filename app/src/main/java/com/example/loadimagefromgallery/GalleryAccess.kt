package com.example.loadimagefromgallery

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore




class GalleryAccess {

    fun getAllGalleryFolderNames(context: Context)  {
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
    }
}