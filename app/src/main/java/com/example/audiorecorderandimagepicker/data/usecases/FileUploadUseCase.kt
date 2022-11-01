package com.example.audiorecorderandimagepicker.data.usecases

import com.example.audiorecorderandimagepicker.data.network.UploadResponse
import com.example.audiorecorderandimagepicker.data.network.common.ApiException
import com.example.audiorecorderandimagepicker.data.network.common.Resource
import com.example.audiorecorderandimagepicker.data.repository.UploadFileRepository
import com.example.audiorecorderandimagepicker.ui.home.AttachmentAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FileUploadUseCase @Inject constructor(private val uploadFileRepository: UploadFileRepository) {

    operator fun invoke(
        content_id: Int,
        comment: String,
        attachments: List<AttachmentAdapter.Attachment>?
    ): Flow<Resource<UploadResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = uploadFileRepository.uploadFiles(
                content_id = content_id,
                comment = comment,
                attachments = attachments
            )
            emit(Resource.Success(data = response))
        } catch (e: ApiException) {
            emit(Resource.Error(e.message))
        } catch (e: Exception) {
            emit(Resource.Error(e.message))
        }
    }
}