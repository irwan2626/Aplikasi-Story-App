package com.example.loginapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.irwan.aplikasistoryapp.R
import com.irwan.aplikasistoryapp.Ui.Add.AddStoryActivity
import com.irwan.aplikasistoryapp.api.Config
import com.irwan.aplikasistoryapp.api.Story
import com.irwan.aplikasistoryapp.databinding.ActivityAddLisStoryBinding
import kotlinx.coroutines.launch

class AddLisStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLisStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLisStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        setupRecyclerView()

        // Receive data from AddStoryActivity
        val description = intent.getStringExtra("description")
        val photoUri = intent.getStringExtra("photoUri")

        if (description != null && photoUri != null) {
            // Display data in RecyclerView or a placeholder view
            val newStory = Story(
                id = "",
                name = "New Story", // Placeholder title
                description = description,
                photoUrl = photoUri,
                createdAt = "",
                lat = null,
                lon = null
            )

            val adapter = StoryAdapter(listOf(newStory)) // Update adapter with new story
            binding.rvStories.adapter = adapter
        } else {
            fetchStories()
        }

        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }


    private fun setupRecyclerView() {
        binding.rvStories.layoutManager = LinearLayoutManager(this)
        binding.rvStories.setHasFixedSize(true)
    }

    private fun fetchStories() {
        val apiService = Config.instance
        val token = "2022-01-08T06:34:18.598Z" // Gantilah dengan token yang valid

        lifecycleScope.launch {
            try {
                val response = apiService.getAllStories(token)
                if (response.isSuccessful && response.body() != null) {
                    val storiesResponse = response.body()!!
                    if (!storiesResponse.error) {
                        val adapter = StoryAdapter(storiesResponse.listStory)
                        binding.rvStories.adapter = adapter
                        binding.progressBar.visibility = View.GONE
                        if (storiesResponse.listStory.isEmpty()) {
                            binding.tvMainEmpty.visibility = View.VISIBLE
                        } else {
                            binding.tvMainEmpty.visibility = View.GONE
                        }
                    } else {
                        showError(storiesResponse.message)
                    }
                } else {
                    showError("Failed to fetch stories.")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvMainEmpty.text = message
        binding.tvMainEmpty.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    inner class StoryAdapter(private val listStory: List<Story>) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

        inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivPhoto: ImageView = itemView.findViewById(R.id.iv_photo)
            val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
            val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
            return StoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
            val story = listStory[position]
            holder.tvTitle.text = story.name
            holder.tvDescription.text = story.description

            // Load the photo using Glide
            Glide.with(holder.itemView.context)
                .load(story.photoUrl)
                .into(holder.ivPhoto)
        }

        override fun getItemCount(): Int = listStory.size
    }
}
