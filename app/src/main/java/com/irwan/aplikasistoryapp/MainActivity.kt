package com.irwan.aplikasistoryapp

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.irwan.aplikasistoryapp.api.Config
import com.irwan.aplikasistoryapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        // Handle Camera Button
        binding.cameraButton.setOnClickListener {
            openImagePicker()
        }

        // Handle Gallery Button
        binding.galleryButton.setOnClickListener {
            openImagePicker()
        }

        // Handle Upload Button
        binding.uploadButton.setOnClickListener {
            val description = binding.descriptionInput.text.toString()
            if (description.isEmpty()) {
                Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (token != null) {
                uploadStory(description, selectedImageUri!!, token)
            } else {
                Toast.makeText(this, "Token not found. Please log in again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            binding.storyImagePreview.setImageURI(selectedImageUri) // Set preview image
        }
    }

    private fun uploadStory(description: String, photoUri: Uri, token: String) {
        val apiService = Config.instance
        lifecycleScope.launch {
            try {
                val file = getFileFromUri(photoUri)
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("photo", file.name, requestFile)
                val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.addNewStory(
                    description = descriptionPart,
                    photo = photoPart,
                    lat = null,
                    lon = null
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (!body.error!!) {
                        Toast.makeText(this@MainActivity, "Story uploaded successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, body.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Upload failed!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp", ".jpg", cacheDir)
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    companion object {
        private const val REQUEST_IMAGE_PICKER = 1001
    }
}

