package com.example.audiorecorderandimagepicker.ui.home

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audiorecorderandimagepicker.data.network.UploadResponse
import com.example.audiorecorderandimagepicker.data.network.common.Resource
import com.example.audiorecorderandimagepicker.data.usecases.FileUploadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val useCase: FileUploadUseCase) : ViewModel() {

    private val _fileUpload = MutableLiveData<Resource<UploadResponse>>()
    //observe this variable for ui updates.
    val fileUpload: LiveData<Resource<UploadResponse>> = _fileUpload

    var cachedAudioUri: Uri? = null
    var comment: String = ""

    var attachments: ArrayList<AttachmentAdapter.Attachment> = ArrayList()

    /***
     * add your api end point and call this method for upload files to server.
     */
    private fun uploadFile() {
        useCase(
            1,
            comment,
            attachments,
        ).onEach {
            when (it) {
                is Resource.Loading -> {
                    _fileUpload.value = Resource.Loading()
                }
                is Resource.Success -> {
                    _fileUpload.value = Resource.Success(it.data)
                }
                is Resource.Error -> {
                    _fileUpload.value = Resource.Error(it.message.toString())
                }
            }
        }.launchIn(viewModelScope)
    }

}