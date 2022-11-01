package com.example.audiorecorderandimagepicker.data.network.common

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Suppress("UNCHECKED_CAST")
interface BaseApi {
    companion object {
        private var instance: Any? = null

        operator fun <T : Any> invoke(classOfT: Class<T>, baseUrl: String): T {
            return (instance ?: buildApi(classOfT, baseUrl)) as T
        }

        private fun <T : Any> buildApi(classOfT: Class<T>, baseUrl: String): T {

            val client = OkHttpClient
                .Builder()
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            return Retrofit
                .Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(classOfT)
        }
    }
}