package com.example.audiorecorderandimagepicker.audiorecorder

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.squareup.picasso.Picasso
import java.io.File

fun deleteFile(filePath: String): Boolean {
    var file: File = File(filePath)
    return file.delete()
}

fun deleteRecursive(fileOrDirectory: File) {
    if (fileOrDirectory.isDirectory) {
        for (child in fileOrDirectory.listFiles()) deleteRecursive(child)
    }
    fileOrDirectory.delete()
}

@Suppress("DEPRECATION")
@SuppressLint("Recycle")
fun Uri.getAudioPath(context: Context): String? {
    val projection = arrayOf(MediaStore.Audio.Media.DATA)
    val cursor: Cursor? = context.contentResolver.query(this, projection, null, null, null)
    return if (cursor != null) {
        // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
        // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
        val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor.moveToFirst()
        cursor.getString(columnIndex)
    } else null
}

fun View.onThrottledClick(
    throttleDelay: Long = 500L,
    onClick: (View) -> Unit
) {
    setOnClickListener {
        onClick(this)
        isClickable = false
        postDelayed({ isClickable = true }, throttleDelay)
    }
}


/***
 * Checking nullability
 */
fun Any?.isNull() = this == null

/***
 * Checking nullability
 */
fun Any?.isNotNull() = this != null


fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun ImageView.loadFromUri(path: String) {
    Picasso.get().load(File(path)).into(this)
}

fun ImageView.loadFromUrl(url: String) {
    Picasso.get().load(url).into(this)
}

/**
 * To check the user has granted the [permissions]
 * it will returns true if the all [permissions] granted other wise returns false
 *
 * @return the result in [Boolean]
 */
fun Context.hasPermissions(permissions: Array<String>): Boolean {
    val notGrantedPermissions = ArrayList<String>()
    permissions.forEach { permission ->
        if (ActivityCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notGrantedPermissions.add(permission)
        }
    }
    return notGrantedPermissions.size <= 0
}
