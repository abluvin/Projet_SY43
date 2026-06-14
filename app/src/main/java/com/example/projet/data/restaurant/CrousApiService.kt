package com.example.projet.data.restaurant

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

interface CrousApiService {
    @GET("v1/restaurants/{id}/menu")
    suspend fun getMenuList(@Path("id") restaurantId: String): CrousResponse<List<MenuDay>>

    @GET("v1/restaurants/{id}/menu/{date}")
    suspend fun getMenuByDate(
        @Path("id") restaurantId: String,
        @Path("date") date: String
    ): CrousResponse<MenuDay>

    companion object {
        private const val BASE_URL = "https://api.croustillant.menu/"

        fun create(): CrousApiService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            val contentType = "application/json".toMediaType()
            val json = Json { 
                ignoreUnknownKeys = true 
                coerceInputValues = true
            }

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(CrousApiService::class.java)
        }
    }
}
