package com.example.audiorecorderandimagepicker.data.repository

import com.example.audiorecorderandimagepicker.data.network.Api
import com.example.audiorecorderandimagepicker.data.network.UploadResponse
import com.example.audiorecorderandimagepicker.data.network.common.Media
import com.example.audiorecorderandimagepicker.data.network.common.SafeApiRequest
import com.example.audiorecorderandimagepicker.data.network.common.toRequestBody
import com.example.audiorecorderandimagepicker.ui.home.AttachmentAdapter
import com.example.audiorecorderandimagepicker.ui.home.AttachmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import java.io.File
import javax.inject.Inject


class UploadFileRepository @Inject constructor(
    private val api: Api
) : SafeApiRequest() {


    suspend fun uploadFiles(
        content_id: Int,
        comment: String,
        attachments: List<AttachmentAdapter.Attachment>?
    ): UploadResponse {
        return withContext(Dispatchers.IO) {
            val mediaList = ArrayList<Media>()
            attachments?.forEach { it ->
                val attachmentType = when (it.type) {
                    AttachmentType.AUDIO -> "audio"
                    AttachmentType.IMAGE -> "image"
                }
                mediaList.add(
                    Media(
                        attachmentType,
                        File(it.path),
                        MediaType.parse("multipart/form-data")
                    )
                )
            }
            val response = safeApiRequest {
                api.uploadFiles(
                    token = "Bearer <Your Token>",
                    content_id = content_id,
                    comment = comment.toRequestBody(),
                    attachments = Media.mediaListToMultipart(mediaList)
                )
            }
            response
        }
    }
}