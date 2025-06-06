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
import androidx.recyclerview.widget.RecyclerView // Import RecyclerView
import com.example.mymusicapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val songList = ArrayList<Song>() // Change to mutable ArrayList
    private lateinit var songAdapter: SongAdapter

    // Pagination variables
    private var currentPage = 1
    private val songsPerPage = 7 // Adjust this number as needed
    private var isLoading = false // To prevent multiple simultaneous loads
    private var allSongsLoaded = false // To know if all songs have been fetched from the API

    // Base URL for your API
    private val BASE_URL = "https://amit0987-song-json-api-hf.hf.space/" // IMPORTANT: Replace with your server's base URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView() // Set up RecyclerView before fetching data
        fetchSongsFromServer() // Call the function to fetch initial songs from the server
        setupSearchBar()
    }

    private fun fetchSongsFromServer() {
        if (isLoading || allSongsLoaded) {
            return // Prevent multiple loads or loading if all songs are already fetched
        }

        isLoading = true // Set loading flag
        binding.progressBar.visibility = View.VISIBLE // Show loading indicator (add a ProgressBar to activity_main.xml)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiService.getSongs(page = currentPage, limit = songsPerPage)
                if (response.isSuccessful && response.body() != null) {
                    val newSongs = response.body()!!
                    if (newSongs.isNotEmpty()) {
                        val startPosition = songList.size
                        songList.addAll(newSongs) // Add new songs to the existing list
                        songAdapter.addSongs(newSongs) // Inform the adapter about new songs
                        // No need for notifyDataSetChanged() here if using addSongs
                        currentPage++ // Increment page for the next load
                    } else {
                        allSongsLoaded = true // No more songs to load
                        Toast.makeText(this@MainActivity, "All songs loaded", Toast.LENGTH_SHORT).show()
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
                isLoading = false // Reset loading flag
                binding.progressBar.visibility = View.GONE // Hide loading indicator
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvSongList.layoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter(songList) { clickedSong, position ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                // Pass the current song index and the entire loaded songList
                putExtra("current_song_index", songList.indexOf(clickedSong))
                putExtra("song_list", ArrayList(songList))
            }
            startActivity(intent)
        }
        binding.rvSongList.adapter = songAdapter

        // Add scroll listener for pagination
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
                        fetchSongsFromServer() // Load more data
                    }
                }
            }
        })
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                // IMPORTANT: When filtering, you should filter the *entire* songList, not just the currently loaded page.
                // If you filter only the current page, the user might not find songs on other pages.
                // This filtering logic should ideally happen on the server for large datasets,
                // but for client-side filtering, ensure 'songList' contains all loaded songs.
                filterSongs(query)
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
        }
    }

    private fun filterSongs(query: String) {
        val filteredList = if (query.isBlank()) {
            songList // Use the full songList for displaying all songs when search is empty
        } else {
            songList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }
        // Update the adapter with the filtered list.
        // For filtering with pagination, you might need a more sophisticated approach,
        // potentially fetching filtered results from the API directly.
        // For now, this filters only the songs already loaded client-side.
        songAdapter.updateList(filteredList)
    }
}