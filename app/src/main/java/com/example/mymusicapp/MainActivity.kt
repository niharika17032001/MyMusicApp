package com.example.mymusicapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymusicapp.databinding.ActivityMainBinding
import org.json.JSONArray
import java.io.IOException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var songList: List<Song>
    private lateinit var songAdapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSongList()
        setupRecyclerView()
        setupSearchBar()
    }

    private fun setupSongList() {
        songList = loadSongsFromJSON()
    }

    private fun loadSongsFromJSON(): List<Song> {
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
            // Handle the error, maybe load a default list or show a message
        } catch (jsonException: Exception) {
            jsonException.printStackTrace()
            // Handle JSON parsing error
        }
        return songs
    }

    private fun setupRecyclerView() {
        binding.rvSongList.layoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter(ArrayList(songList)) { clickedSong, position ->
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