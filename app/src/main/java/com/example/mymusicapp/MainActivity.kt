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

    // Pagination variables for main list
    private var currentPage = 1
    private val songsPerPage = 5
    private var isLoading = false
    private var allSongsLoaded = false

    private var searchJob: Job? = null // For debouncing search input

    // Base URL for your API
    private val BASE_URL = "https://amit0987-song-json-api-hf.hf.space/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchSongsFromServer() // Initial load of main song list
        setupSearchBar()
    }

    private fun fetchSongsFromServer() {
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
                    query = null // Do not pass search query for the main list
                )
                if (response.isSuccessful && response.body() != null) {
                    val newSongs = response.body()!!
                    if (newSongs.isNotEmpty()) {
                        val startPosition = songList.size
                        songList.addAll(newSongs)
                        songAdapter.addSongs(newSongs)
                        currentPage++
                    } else {
                        allSongsLoaded = true
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
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvSongList.layoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter(songList) { clickedSong, position ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("current_song_index", songList.indexOf(clickedSong))
                putExtra("song_list", ArrayList(songList))
            }
            startActivity(intent)
        }
        binding.rvSongList.adapter = songAdapter

        // Keep the scroll listener for main list pagination
        binding.rvSongList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !allSongsLoaded) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= songsPerPage
                    ) {
                        fetchSongsFromServer() // Load more main list data (no query)
                    }
                }
            }
        })
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                val query = s.toString().trim()

                searchJob = lifecycleScope.launch {
                    delay(500) // Debounce
                    if (query.isNotEmpty()) {
                        // Launch SearchActivity with the query
                        val intent = Intent(this@MainActivity, SearchActivity::class.java).apply {
                            putExtra("search_query", query)
                        }
                        startActivity(intent)
                        // Optionally clear search bar after launching new activity
                        // binding.etSearch.text.clear()
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
}