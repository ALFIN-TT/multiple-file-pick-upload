package com.example.audiorecorderandimagepicker.data.network.common

import android.text.TextUtils
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

data class Media(val fileType: String, val file: File?, val mediaType: MediaType?) {
    companion object {
        fun mediaListToMultipart(mediaList: List<Media>): List<MultipartBody.Part> {
            val list = ArrayList<MultipartBody.Part>()
            for (i in mediaList.indices) {
                mediaList[i].let {
                    if (!TextUtils.isEmpty(it.fileType))
                        list.add(
                            MultipartBody.Part.createFormData(
                                "attachments[$i][type]",
                                it.fileType
                            )
                        )
                    if (it.file != null) {
                        val requestFile = RequestBody.create(
                            it.mediaType,
                            it.file
                        )
                        list.add(
                            MultipartBody.Part.createFormData(
                                "attachments[$i][file]",
                                it.file.name,
                                requestFile
                            )
                        )
                    }
                }
            }
            return list
        }
    }
}