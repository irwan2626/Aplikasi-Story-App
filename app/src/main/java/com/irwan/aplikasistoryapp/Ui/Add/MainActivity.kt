package com.irwan.aplikasistoryapp.Ui.Add

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.loginapp.AddLisStoryActivity
import com.irwan.aplikasistoryapp.R
import com.irwan.aplikasistoryapp.Ui.ViewModel.MainViewModel
import com.irwan.aplikasistoryapp.api.ApiClient
import com.irwan.aplikasistoryapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var token: String
    private lateinit var mainViewModel: MainViewModel
    private var selectedImageUri: Uri? = null
        set(value) {
            binding.storyImagePreview.setImageURI(value)
        }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            // Meminta izin kamera
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }


    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
        } else {
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create a file to store the image
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.irwan.aplikasistoryapp.fileprovider", // Ganti dengan authority aplikasi Anda
                    it
                )
                selectedImageUri = it.toUri()
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureLauncher.launch(takePictureIntent)
            }
        }
    }


    private fun createImageFile(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timestamp}_", /* Prefix */
            ".jpg", /* Suffix */
            storageDir /* Directory */
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)




        binding.cameraButton.setOnClickListener {
            startTakePhoto()
        }

        binding.galleryButton.setOnClickListener {
            openImagePicker()
        }

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

            if (token.isNotEmpty()) {
                uploadStory(description, selectedImageUri!!, token)
            } else {
                Toast.makeText(this, "Token not found. Please log in again.", Toast.LENGTH_SHORT).show()
            }
        }


    }




    private fun startTakePhoto() {
        takePicturePreviewLauncher.launch(null)
    }

    private val takePicturePreviewLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            binding.storyImagePreview.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            binding.storyImagePreview.setImageURI(uri) // Display the selected image
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            binding.storyImagePreview.setImageURI(selectedImageUri) // Set preview image
        }
    }

    private fun uploadStory(description: String, photoUri: Uri, token: String) {
        val photoFile = getFileFromUri(photoUri)
        val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, requestFile)
        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())

        mainViewModel.upload(photoPart, descriptionPart, token)

        // Observing upload status
        mainViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        mainViewModel.message.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (message == "Story uploaded successfully") {
                val intent = Intent(this@MainActivity, AddLisStoryActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }



    private fun getFileFromUri(uri: Uri): File {
        val contentResolver = applicationContext.contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: throw IOException("Unable to open input stream")
        val tempFile = File.createTempFile("temp", ".jpg", cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return tempFile
    }

    companion object {
        private const val REQUEST_IMAGE_PICKER = 1001
    }
}



