package com.example.mymusicapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapp.databinding.ActivitySearchBinding // You'll need to create this binding class
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val searchResultsList = ArrayList<Song>()
    private lateinit var searchSongAdapter: SongAdapter

    // Pagination variables for search results
    private var currentPage = 1
    private val songsPerPage = 5
    private var isLoading = false
    private var allSearchResultsLoaded = false

    private var currentSearchQuery: String? = null

    // Base URL for your API (same as in MainActivity)
    private val BASE_URL = "https://amit0987-song-json-api-hf.hf.space/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve search query from the Intent
        currentSearchQuery = intent.getStringExtra("search_query")
        binding.tvSearchResultsTitle.text = "Results for: \"${currentSearchQuery ?: ""}\""

        if (currentSearchQuery.isNullOrBlank()) {
            Toast.makeText(this, "No search query provided.", Toast.LENGTH_SHORT).show()
            finish() // Close if no query
            return
        }

        setupRecyclerView()
        fetchSearchResults(currentSearchQuery) // Initial fetch for the search query
    }

    private fun fetchSearchResults(query: String?) {
        if (isLoading || allSearchResultsLoaded) {
            return
        }

        isLoading = true
        binding.progressBarSearch.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiService.getSongs(
                    page = currentPage,
                    limit = songsPerPage,
                    query = query // Pass the search query to the API
                )
                if (response.isSuccessful && response.body() != null) {
                    val newSongs = response.body()!!
                    if (newSongs.isNotEmpty()) {
                        val startPosition = searchResultsList.size
                        searchResultsList.addAll(newSongs)
                        searchSongAdapter.addSongs(newSongs) // Use addSongs to append
                        currentPage++
                    } else {
                        allSearchResultsLoaded = true
                        Toast.makeText(this@SearchActivity, "All matching songs loaded", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SearchActivity, "Failed to fetch search results: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this@SearchActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } finally {
                isLoading = false
                binding.progressBarSearch.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        searchSongAdapter = SongAdapter(searchResultsList) { clickedSong, position ->
            // Handle click on a search result song.
            // You might want to pass the entire searchResultsList or just the clicked song
            // to the PlayerActivity. Be mindful of performance if searchResultsList is huge.
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("current_song_index", searchResultsList.indexOf(clickedSong))
                putExtra("song_list", ArrayList(searchResultsList)) // Pass the search results list
            }
            startActivity(intent)
        }
        binding.rvSearchResults.adapter = searchSongAdapter

        // Add scroll listener for pagination within search results
        binding.rvSearchResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !allSearchResultsLoaded) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= songsPerPage // Ensure there's enough items to potentially load more
                    ) {
                        fetchSearchResults(currentSearchQuery) // Load more search results
                    }
                }
            }
        })
    }
}