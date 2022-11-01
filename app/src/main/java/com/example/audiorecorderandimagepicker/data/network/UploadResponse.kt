package com.example.audiorecorderandimagepicker.data.network

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UploadResponse(
    @SerializedName("message")
    var message: String?
)