package com.irwan.aplikasistoryapp.Ui.Login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.irwan.aplikasistoryapp.R
import com.irwan.aplikasistoryapp.Ui.Add.MainActivity
import com.irwan.aplikasistoryapp.Ui.Register.RegisterActivity
import com.irwan.aplikasistoryapp.api.LoginRq
import com.irwan.aplikasistoryapp.api.LoginResult
import com.irwan.aplikasistoryapp.api.ApiClient
import com.irwan.aplikasistoryapp.api.Register

import com.irwan.aplikasistoryapp.databinding.ActivityLoginBinding
import com.irwan.aplikasistoryapp.model.User
import kotlinx.coroutines.launch


class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        retrieveLastSession()?.let {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        setLoginButtonEnable()

        binding.apply {
            edLoginEmail.doOnTextChanged { _, _, _, _ ->
                setLoginButtonEnable()
            }
            edLoginPassword.doOnTextChanged { text, _, _, _ ->
                if (text != null && text.length < 8) {
                    binding.edLoginPassword.error = getString(R.string.error_short_password)
                } else {
                    binding.edLoginPassword.error = null
                }
            }
            btnLogin.setOnClickListener {
                val email = binding.edLoginEmail.text.toString()
                val password = binding.edLoginPassword.text.toString()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, 0)

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    loginUser(email, password)
                } else {
                    Toast.makeText(this@Login, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }

            btnLogin.setOnClickListener {
                val email = binding.edLoginEmail.text.toString()
                val password = binding.edLoginPassword.text.toString()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, 0)

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    loginUser(email, password)
                } else {
                    Toast.makeText(this@Login, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }

            textRegister.setOnClickListener {
                val intent = Intent(this@Login, RegisterActivity::class.java)
                startActivity(intent)
            }
        }

    }

    private fun loginUser(email: String, password: String) {
        showLoading(true)
        val apiService = ApiClient.instance
        val request = LoginRq(email, password)

        lifecycleScope.launch {
            try {
                val response = apiService.loginUser(request)
                showLoading(false)

                // Jika login berhasil
                if (!response.error) {
                    saveUserDetails(response.loginResult)
                    Toast.makeText(this@Login, "Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Login, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Jika login gagal, cek jika pengguna belum terdaftar
                    if (response.message == "User not registered") {
                        // Arahkan ke halaman registrasi jika user belum terdaftar
                        val intent = Intent(this@Login, Register::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Tampilkan pesan error jika login gagal karena alasan lain
                        showError(true, response.message)
                    }
                }
            } catch (e: Exception) {
                showError(true, "Error: ${e.message}")
                showLoading(false)
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

    private fun retrieveLastSession(): User? {
        val id    = sharedPreferences.getString("userId", null)
        val name  = sharedPreferences.getString("name", null)
        val token = sharedPreferences.getString("token", null)
        if (id != null && name != null && token != null) {
            return User(id, name, token)
        }
        return null
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(isError: Boolean, message: String) {
        if (isError) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLoginButtonEnable() {
        val emailResult = binding.edLoginEmail.text
        val passwordResult = binding.edLoginPassword.text
        binding.btnLogin.isEnabled = !emailResult.isNullOrBlank() && !passwordResult.isNullOrBlank()
    }
}




