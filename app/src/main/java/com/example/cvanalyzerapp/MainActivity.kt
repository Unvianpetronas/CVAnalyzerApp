package com.example.cvanalyzerapp


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var btnSelectFile: Button
    private lateinit var tvFileName: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnLogout: Button
    private lateinit var sessionManager: SessionManager

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadAndAnalyzeCv(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)
        btnSelectFile = findViewById(R.id.btnSelectFile)
        tvFileName = findViewById(R.id.tvFileName)
        progressBar = findViewById(R.id.progressBar)
        btnLogout = findViewById(R.id.btnLogout)

        btnSelectFile.setOnClickListener {
            filePickerLauncher.launch("application/pdf")
        }

        btnLogout.setOnClickListener {
            sessionManager.clearAuthToken()
            val intent = Intent(this, LoginActivity::class.java)
            // Xóa tất cả các activity trước đó khỏi stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun uploadAndAnalyzeCv(fileUri: Uri) {
        tvFileName.text = getFileName(fileUri)
        progressBar.visibility = View.VISIBLE
        btnSelectFile.isEnabled = false

        val apiService = ApiClient.getRetrofitInstance(this).create(ApiService::class.java)

        val inputStream = contentResolver.openInputStream(fileUri)
        val requestBody = inputStream?.readBytes()?.toRequestBody("application/pdf".toMediaTypeOrNull())
        inputStream?.close()

        if (requestBody == null) {
            showError("cannot read file.")
            resetUI()
            return
        }

        val body = MultipartBody.Part.createFormData("file", getFileName(fileUri), requestBody)

        apiService.uploadCv(body).enqueue(object : Callback<CvAnalysisResult> {
            override fun onResponse(call: Call<CvAnalysisResult>, response: Response<CvAnalysisResult>) {
                if (response.isSuccessful) {
                    showResultDialog(response.body())
                } else {
                    showError(" server error : ${response.code()}. token is expired, please try again.")
                }
                resetUI()
            }

            override fun onFailure(call: Call<CvAnalysisResult>, t: Throwable) {
                showError("connection error: ${t.message}")
                resetUI()
            }
        })
    }

    private fun showResultDialog(result: CvAnalysisResult?) {
        if (result == null) {
            showError("No valid results received.")
            return
        }
        val message = """
        Score: ${result.score ?: "N/A"}/100

        Strengths:
        ${result.strengths ?: "Do not have"}

        Weaknesses:
        ${result.weaknesses ?: "Do not have"}

        Detected skills: 
        ${result.detected_skills?.joinToString(", ") ?: "Do not have"}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Result")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun resetUI() {
        progressBar.visibility = View.GONE
        btnSelectFile.isEnabled = true
        tvFileName.text = "No files selected yet"
    }

    private fun getFileName(uri: Uri): String {
        var fileName: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val colIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if(colIndex >= 0) fileName = cursor.getString(colIndex)
                }
            }
        }
        return (fileName ?: uri.path?.last() ?: "unknown.pdf") as String
    }
}
