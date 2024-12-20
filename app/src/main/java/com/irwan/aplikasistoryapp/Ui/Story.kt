/*
package com.irwan.aplikasistoryapp.ui

import StoryAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.irwan.aplikasistoryapp.R
import com.irwan.aplikasistoryapp.Ui.Add.AddStoryActivity
import com.irwan.aplikasistoryapp.api.Config
import com.irwan.aplikasistoryapp.databinding.ActivityStoryBinding
import kotlinx.coroutines.launch

class Story : AppCompatActivity() {

    private lateinit var binding: ActivityStoryBinding
    private val storyAdapter by lazy { StoryAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFab()
        fetchStories()
    }

    private fun setupRecyclerView() {
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@Story)
            adapter = storyAdapter
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchStories() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = Config.instance.Stories()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (!body.error) {
                        storyAdapter.submitList(body.listStory)
                    } else {
                        showEmptyMessage(true)
                    }
                } else {
                    Toast.makeText(this@Story, getString(R.string.fetch_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Story, getString(R.string.error_generic, e.message), Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmptyMessage(isVisible: Boolean) {
        binding.tvMainEmpty.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}
*/
