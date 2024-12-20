package com.irwan.aplikasistoryapp.Ui.Add

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
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.irwan.aplikasistoryapp.R
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
        fetchStories()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.fab.setOnClickListener {
            // Navigate to Add Story Activity
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        binding.rvStories.layoutManager = LinearLayoutManager(this)
        binding.rvStories.setHasFixedSize(true)
    }

    private fun fetchStories() {
        val apiService = Config.instance

        lifecycleScope.launch {
            try {
                val response = apiService.getAllStories()
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

    class StoryAdapter(listStory: List<Story>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        // ViewHolder class for holding views for each story item
        inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivPhoto: ImageView = itemView.findViewById(R.id.iv_photo)
            val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
            val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        }

        // Inflates the item layout and returns a StoryViewHolder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
            return StoryViewHolder(view)
        }

        // Binds the data to the views in the StoryViewHolder
        override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
            val story = story[position]
            holder.tvTitle.text = story.name
            holder.tvDescription.text = story.description

            // Load the photo using Glide
            Glide.with(holder.itemView.context)
                .load(story.photoUrl)
                .into(holder.ivPhoto)
        }

        // Returns the total number of stories
        override fun getItemCount(): Int = stories.size

    }
}
