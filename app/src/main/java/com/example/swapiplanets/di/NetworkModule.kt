package com.example.swapiplanets.di

import com.example.swapiplanets.data.network.SwapiApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val PRIMARY_BASE_URL = "https://swapi.dev/api/"
    private const val FALLBACK_BASE_URL = "https://swapi.py4e.com/api/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named("primary")
    fun providePrimaryRetrofit(
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(PRIMARY_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("fallback")
    fun provideFallbackRetrofit(
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(FALLBACK_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("primary")
    fun providePrimarySwapiApi(@Named("primary") retrofit: Retrofit): SwapiApi =
        retrofit.create(SwapiApi::class.java)

    @Provides
    @Singleton
    @Named("fallback")
    fun provideFallbackSwapiApi(@Named("fallback") retrofit: Retrofit): SwapiApi =
        retrofit.create(SwapiApi::class.java)
}