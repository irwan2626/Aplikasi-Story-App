package com.irwan.aplikasistoryapp.Ui.Register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.irwan.aplikasistoryapp.api.Config
import com.irwan.aplikasistoryapp.R
import com.irwan.aplikasistoryapp.Ui.Add.AddStoryActivity
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

        binding.btnRegister.setOnClickListener {
            val username = binding.edRegisterName.text.toString().trim()
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()

            if (validateInput(username, email, password)) {
                showLoading(true)
                registerUser(username, email, password)
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() -> {
                showToast(getString(R.string.error_empty_name))
                false
            }
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast(getString(R.string.error_invalid_email))
                false
            }
            password.length < 6 -> {
                showToast(getString(R.string.error_short_password))
                false
            }
            else -> true
        }
    }


    private fun registerUser(name: String, email: String, password: String) {
        val apiService = Config.instance
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
        val intent = Intent(this@RegisterActivity, AddStoryActivity::class.java)
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


