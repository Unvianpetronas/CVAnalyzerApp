package com.example.cvanalyzerapp

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // <<<< CỰC KỲ QUAN TRỌNG >>>>
    // THAY ĐỔI ĐỊA CHỈ IP NÀY BẰNG ĐỊA CHỈ IP CỦA BACKEND
    private const val BASE_URL = "http://192.168.1.201:5001"

    fun getRetrofitInstance(context: Context): Retrofit {
        val sessionManager = SessionManager(context)
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
