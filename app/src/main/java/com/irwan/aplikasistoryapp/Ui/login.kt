package com.irwan.aplikasistoryapp.Ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.irwan.aplikasistoryapp.api.LoginRq
import com.irwan.aplikasistoryapp.api.LoginResult
import com.irwan.aplikasistoryapp.MainActivity
import com.irwan.aplikasistoryapp.api.Config

import com.irwan.aplikasistoryapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch


class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        binding.textRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        val apiService = Config.instance
        val request = LoginRq(email, password)

        lifecycleScope.launch {
            try {
                val response = apiService.loginUser(request)
                if (!response.error) {
                    saveUserDetails(response.loginResult)
                    Toast.makeText(this@Login, "Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Login, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@Login, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Login, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserDetails(loginResult: LoginResult?) {
        loginResult?.let {
            val editor = sharedPreferences.edit()
            editor.putString("userId", it.userId)
            editor.putString("name", it.name)
            editor.putString("token", it.token)
            editor.apply()
        }
    }
}



