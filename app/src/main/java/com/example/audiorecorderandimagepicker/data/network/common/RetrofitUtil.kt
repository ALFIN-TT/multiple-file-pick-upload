package com.example.audiorecorderandimagepicker.data.network.common

import okhttp3.MediaType
import okhttp3.RequestBody


fun <T> T.toRequestBody(): RequestBody {
    return RequestBody.create(MediaType.parse("multipart/form-data"), this.toString())
}

fun <T> T?.toRequestBodyNullable(): RequestBody? {
    return if (this != null) {
        RequestBody.create(
            MediaType.parse("multipart/form-data"),
            this.toString()
        )
    } else null
}