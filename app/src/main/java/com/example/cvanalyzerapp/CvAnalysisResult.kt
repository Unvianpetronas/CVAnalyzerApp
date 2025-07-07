package com.example.cvanalyzerapp

// Lớp này định nghĩa cấu trúc dữ liệu mà chúng ta nhận về từ Backend
data class CvAnalysisResult(
    val score: Int?,
    val strengths: String?,
    val weaknesses: String?,
    val detected_skills: List<String>?
)
