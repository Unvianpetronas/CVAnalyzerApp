package com.example.cvanalyzerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        // Kiểm tra xem người dùng đã đăng nhập chưa. Nếu có, vào thẳng MainActivity.
        if (sessionManager.fetchAuthToken() != null) {
            goToMainActivity()
            return // Rất quan trọng, ngăn không cho code bên dưới chạy
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val apiService = ApiClient.getRetrofitInstance(this).create(ApiService::class.java)
            val request = AuthRequest(email, password)

            apiService.login(request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful && response.body()?.token != null) {
                        val token = response.body()!!.token!!
                        sessionManager.saveAuthToken(token)
                        goToMainActivity()
                    } else {
                        val errorMsg = response.body()?.error ?: "Email or password not correct"
                        Toast.makeText(this@LoginActivity, "Lỗi: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "connection error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Đóng LoginActivity để người dùng không thể quay lại bằng nút back
    }
}
