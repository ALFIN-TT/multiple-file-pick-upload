package com.example.audiorecorderandimagepicker.data.di

import com.example.audiorecorderandimagepicker.data.network.Api
import com.example.audiorecorderandimagepicker.data.network.common.BaseApi
import com.example.audiorecorderandimagepicker.data.repository.UploadFileRepository
import com.example.audiorecorderandimagepicker.data.usecases.FileUploadUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesApi(): Api =
        BaseApi(Api::class.java, "http://your.baseurl")


    @Provides
    @Singleton
    fun provideUploadFileRepository(api: Api) = UploadFileRepository(api)

    @Provides
    fun provideSpeakersUseCase(uploadFileRepository: UploadFileRepository) =
        FileUploadUseCase(uploadFileRepository)

}