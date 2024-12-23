package com.irwan.aplikasistoryapp.Ui.ViewModel

import android.media.metrics.Event
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irwan.aplikasistoryapp.api.ApiClient
import com.irwan.aplikasistoryapp.api.ApiService
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun upload(photo: MultipartBody.Part, des: RequestBody, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.addNewStory(
                    description = des,
                    photo = photo,
                    lat = null,
                    lon = null
                )
                _isLoading.value = false

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        _message.value = "Story uploaded successfully"
                    } else {
                        _message.value = responseBody?.message ?: "Unknown error occurred"
                    }
                } else {
                    _message.value = "Error: ${response.message()}"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _message.value = "Failure: ${e.message}"
            }
        }
    }
}

