package com.example.mymusicapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val songList = ArrayList<Song>()
    private lateinit var songAdapter: SongAdapter

    // Pagination variables
    private var currentPage = 1
    private val songsPerPage = 20
    private var isLoading = false
    private var allSongsLoaded = false // Tracks if all songs matching current criteria are loaded

    // Search variables
    private var searchJob: Job? = null // For debouncing search
    private var currentSearchQuery: String? = null // Stores the active search query

    // Base URL for your API
    private val BASE_URL = "https://amit0987-song-json-api-hf.hf.space/" // IMPORTANT: Replace with your server's base URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchSongsFromServer(currentSearchQuery) // Initial load
        setupSearchBar()
    }

    private fun fetchSongsFromServer(query: String? = null) {
        if (isLoading || allSongsLoaded) {
            return
        }

        isLoading = true
        binding.progressBar.visibility = View.VISIBLE

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
                    query = query // Pass the search query
                )
                if (response.isSuccessful && response.body() != null) {
                    val newSongs = response.body()!!
                    if (newSongs.isNotEmpty()) {
                        // If this is the first page of a new search, clear existing songs
                        if (currentPage == 1) {
                            songList.clear()
                            songAdapter.updateList(emptyList()) // Clear adapter too
                        }
                        songList.addAll(newSongs)
                        songAdapter.addSongs(newSongs) // Add new songs
                        currentPage++
                    } else {
                        // No more songs for the current query or page
                        if (currentPage == 1) { // If no songs found on first page, show empty state
                            songList.clear()
                            songAdapter.updateList(emptyList())
                            Toast.makeText(this@MainActivity, "No songs found for '$query'", Toast.LENGTH_SHORT).show()
                        }
                        allSongsLoaded = true
                        if (query.isNullOrBlank()) {
                            Toast.makeText(this@MainActivity, "All songs loaded", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "All matching songs loaded", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch songs: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this@MainActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvSongList.layoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter(songList) { clickedSong, position ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                // Pass the current song index and the entire loaded songList
                // NOTE: For player activity, passing the entire songList might be problematic
                // if you have millions of songs. Consider passing only necessary info or
                // fetching the song details in PlayerActivity if songList gets extremely large.
                putExtra("current_song_index", songList.indexOf(clickedSong))
                putExtra("song_list", ArrayList(songList))
            }
            startActivity(intent)
        }
        binding.rvSongList.adapter = songAdapter

        binding.rvSongList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Check if we are at the end of the list and not already loading or all songs loaded
                if (!isLoading && !allSongsLoaded) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= songsPerPage // Ensure there's enough items to potentially load more
                    ) {
                        fetchSongsFromServer(currentSearchQuery) // Load more data for the current query
                    }
                }
            }
        })
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel() // Cancel previous search job
                val query = s.toString().trim() // Trim whitespace

                // Debounce search to prevent excessive API calls
                searchJob = lifecycleScope.launch {
                    delay(500) // Wait for 500ms after user stops typing
                    if (query != currentSearchQuery) { // Only search if query changed
                        currentSearchQuery = query
                        resetPaginationAndFetch() // Reset pagination and fetch with new query
                    }
                }
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
        }
    }

    // New function to reset pagination when a new search starts
    private fun resetPaginationAndFetch() {
        currentPage = 1
        isLoading = false
        allSongsLoaded = false
        // The fetchSongsFromServer will handle clearing the list if currentPage == 1
        fetchSongsFromServer(currentSearchQuery)
    }

    // The old client-side filterSongs is no longer needed for primary search
    // It could potentially be used for very light, in-memory filtering if needed for other purposes.
    /*
    private fun filterSongs(query: String) {
        val filteredList = if (query.isBlank()) {
            songList
        } else {
            songList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }
        songAdapter.updateList(filteredList)
    }
    */
}