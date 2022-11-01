package com.example.audiorecorderandimagepicker.data.network.common

import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

abstract class SafeApiRequest {

    suspend fun <T : Any> safeApiRequest(call: suspend () -> Response<T>): T {
        val response = call.invoke()
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            val error = response.errorBody()?.string()
            val message = StringBuilder()
            error.let {
                try {
                    message.append(JSONObject(it).getString("error"))
                } catch (e: JSONException) {
                    message.append(e.message)
                    e.printStackTrace()
                } catch (e: Exception) {
                    message.append(e.message)
                    e.printStackTrace()
                }
                throw ApiException(message.toString())
            }
        }
    }
}