package com.example.audiorecorderandimagepicker.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface Api {


    /**
     * This end point like 'your_api/12/endpoint'
     *
     *  AUTHORIZATION - Bearer Token
     *
     *  HEADERS
     *    Accept : application/json
     *
     *  PATH : ID of topic, in this example 12
     *
     *  BODY-  formdata
     *
     *    comment : 'abcde'
     *    attachments[0][type] : 'image' //or audio
     *    attachments[0][file] : Multipart files
     *
     *    attachments[1][type] : 'image' //or audio
     *    attachments[1][file] : Multipart files
     */
    @Multipart
    @POST("your_api/{path_id}/endpoint")
    suspend fun uploadFiles(
        @Header("Authorization") token: String,
        @Path("path_id") content_id: Int,
        @Part("comment") comment: RequestBody,
        @Part attachments: List<MultipartBody.Part>?,
    ): Response<UploadResponse>

}