package com.irwan.aplikasistoryapp.Ui.Register

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.irwan.aplikasistoryapp.api.ApiClient
import com.irwan.aplikasistoryapp.R
import com.irwan.aplikasistoryapp.Ui.Login.Login
import com.irwan.aplikasistoryapp.api.Register
import com.irwan.aplikasistoryapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException


class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.edRegisterPassword.doOnTextChanged { text, _, _, _ ->
            if (text != null && text.length < 8) {
                binding.edRegisterPassword.error = getString(R.string.error_short_password)
            } else {
                binding.edRegisterPassword.error = null
            }
        }

        supportActionBar?.hide()

        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString().trim()
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()

            if (validateInput(name, email, password)) {
                showLoading(true)
                registerUser(name, email, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            // Kembali ke halaman login
            finish()
        }

        binding.edRegisterName.doOnTextChanged { _, _, _, _ -> setRegisterButtonEnable() }
        binding.edRegisterEmail.doOnTextChanged { _, _, _, _ -> setRegisterButtonEnable() }
        binding.edRegisterPassword.doOnTextChanged { _, _, _, _ -> setRegisterButtonEnable() }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() -> {
                showToast(getString(R.string.error_empty_name))
                false
            }
            email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast(getString(R.string.error_invalid_email))
                false
            }
            password.length < 8 -> {
                showToast(getString(R.string.error_short_password))
                false
            }
            else -> true
        }
    }

    private fun setRegisterButtonEnable() {
        val nameResult = binding.edRegisterName.text
        val emailResult = binding.edRegisterEmail.text
        val passwordResult = binding.edRegisterPassword.text

        binding.btnRegister.isEnabled = !nameResult.isNullOrBlank() &&
                !emailResult.isNullOrBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(emailResult).matches() &&
                !passwordResult.isNullOrBlank() &&
                passwordResult.length >= 8
    }

    private fun registerUser(name: String, email: String, password: String) {
        val apiService = ApiClient.instance
        val register = Register(name, email, password)

        lifecycleScope.launch {
            try {
                val response = apiService.registerUser(register)

                if (!response.error) {
                    showToast(getString(R.string.register_success))
                    navigateToMainActivity()
                } else {
                    showToast(response.message)
                }
            } catch (e: HttpException) {
                showToast(getString(R.string.error_generic, e.response()?.message() ?: e.message()))
            } catch (e: Exception) {
                showToast(getString(R.string.error_generic, e.message))
            } finally {
                showLoading(false)
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@RegisterActivity, Login::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
    }
}





