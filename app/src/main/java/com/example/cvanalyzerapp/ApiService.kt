package com.example.cvanalyzerapp

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Data class cho các request đăng ký/đăng nhập
data class AuthRequest(val email: String, val password: String)

// Data class cho các response xác thực
data class AuthResponse(val token: String?, val error: String?, val message: String?)

// Data class cho kết quả phân tích CV

interface ApiService {
    @POST("register")
    fun register(@Body request: AuthRequest): Call<AuthResponse>

    @POST("login")
    fun login(@Body request: AuthRequest): Call<AuthResponse>

    @Multipart
    @POST("upload-cv")
    fun uploadCv(@Part file: MultipartBody.Part): Call<CvAnalysisResult>
}
