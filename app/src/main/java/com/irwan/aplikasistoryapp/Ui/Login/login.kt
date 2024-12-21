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
import com.irwan.aplikasistoryapp.Ui.Add.AddStoryActivity
import com.irwan.aplikasistoryapp.Ui.Register.RegisterActivity
import com.irwan.aplikasistoryapp.api.LoginRq
import com.irwan.aplikasistoryapp.api.LoginResult
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

        supportActionBar?.hide()

        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        setLoginButtonEnable()

        binding.edLoginEmail.doOnTextChanged { _, _, _, _ ->
            setLoginButtonEnable()
        }

        binding.edLoginPassword.doOnTextChanged { text, _, _, _ ->
            if (text != null && text.length < 8) {
                binding.edLoginPassword.error = getString(R.string.error_short_password)
            } else {
                binding.edLoginPassword.error = null
            }
        }


        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        showLoading(true)
        val apiService = Config.instance
        val request = LoginRq(email, password)

        lifecycleScope.launch {
            try {
                val response = apiService.loginUser(request)
                showLoading(false)
                if (!response.error) {
                    saveUserDetails(response.loginResult)
                    Toast.makeText(this@Login, "Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Login, AddStoryActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showError(true, response.message)
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




