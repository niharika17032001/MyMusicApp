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
import com.example.mymusicapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import org.json.JSONArray
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var songList: List<Song>
    private lateinit var songAdapter: SongAdapter

    // Base URL for your API
    private val BASE_URL = "https://amit0987-song-json-api-hf.hf.space/" // IMPORTANT: Replace with your server's base URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchSongsFromServer() // Call the function to fetch songs from the server
        setupRecyclerView() // Set up RecyclerView, it will be updated when data is fetched
        setupSearchBar()
    }

    private fun fetchSongsFromServer() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiService.getSongs()
                if (response.isSuccessful && response.body() != null) {
                    songList = response.body()!!
                    songAdapter.updateList(songList) // Update adapter with fetched songs
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch songs: ${response.code()}", Toast.LENGTH_LONG).show()
                    // Optionally load from assets as a fallback
                    songList = loadSongsFromJSONAsset() // Fallback to local JSON if server fails
                    songAdapter.updateList(songList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error fetching songs: ${e.message}", Toast.LENGTH_LONG).show()
                // Optionally load from assets as a fallback
                songList = loadSongsFromJSONAsset() // Fallback to local JSON on network error
                songAdapter.updateList(songList)
            }
        }
    }

    // Keep this fallback function to load from assets if network fails or for development
    private fun loadSongsFromJSONAsset(): List<Song> {
        val songs = mutableListOf<Song>()
        try {
            val jsonString: String = assets.open("songs.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val songJson = jsonArray.getJSONObject(i)
                val id = songJson.getString("id")
                val title = songJson.getString("title")
                val artist = songJson.getString("artist")
                val albumArtUrl = songJson.getString("albumArtUrl")
                val audioUrl = songJson.getString("audioUrl")
                songs.add(Song(id, title, artist, albumArtUrl, audioUrl))
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            Toast.makeText(this, "Error loading local songs", Toast.LENGTH_SHORT).show()
        } catch (jsonException: Exception) {
            jsonException.printStackTrace()
            Toast.makeText(this, "Error parsing local songs", Toast.LENGTH_SHORT).show()
        }
        return songs
    }


    private fun setupRecyclerView() {
        binding.rvSongList.layoutManager = LinearLayoutManager(this)
        // Initialize adapter with an empty list or a loading indicator initially
        songAdapter = SongAdapter(ArrayList()) { clickedSong, position ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("current_song_index", songList.indexOf(clickedSong))
                putExtra("song_list", ArrayList(songList))
            }
            startActivity(intent)
        }
        binding.rvSongList.adapter = songAdapter
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
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
            songList
        } else {
            songList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }
        songAdapter.updateList(filteredList)
    }
}