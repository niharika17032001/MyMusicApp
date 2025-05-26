package com.example.mymusicapp // CHANGED HERE

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymusicapp.databinding.ActivityMainBinding // CHANGED HERE

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var songList: List<Song>
    private lateinit var songAdapter: SongAdapter // Keep a reference to the adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSongList()
        setupRecyclerView()
        setupSearchBar() // Call the new search bar setup method
    }

    private fun setupSongList() {
        // --- IMPORTANT: Replace with actual working MP3 and Album Art URLs ---
        // You can find free MP3s for testing on sites like zapsplat.com (with attribution)
        // Or host your own small MP3 files.
        // Ensure album art URLs are also publicly accessible.
        songList = listOf(
            Song(
                id = "1",
                title = "Upbeat Acoustic",
                artist = "Zapsplat",
                albumArtUrl = "https://pagalworldmusic.com/downloads/cover/4572576/4572576.jpg",
                audioUrl = "https://pagalworldmusic.com/download.php?title=Aankhon+feat.+Ankur+Tewari-320kbps&path=downloads%2Fhigh%2FGCMTcwNWWnc%2FGCMTcwNWWnc.mp3"
            ),
            Song(
                id = "2",
                title = "Chill Corporate",
                artist = "Zapsplat",
                albumArtUrl = "https://www.zapsplat.com/wp-content/uploads/2015/09/music_zapsplat_chill_corporate.png",
                audioUrl = "https://www.zapsplat.com/wp-content/uploads/2015/09/music_zapsplat_chill_corporate.mp3"
            ),
            Song(
                id = "3",
                title = "Smooth Jazz Loop",
                artist = "Zapsplat",
                albumArtUrl = "https://www.zapsplat.com/wp-content/uploads/2015/09/music_zapsplat_smooth_jazz_loop.png",
                audioUrl = "https://www.zapsplat.com/wp-content/uploads/2015/09/music_zapsplat_smooth_jazz_loop.mp3"
            ),
            Song(
                id = "4",
                title = "Acoustic Folk",
                artist = "Zapsplat",
                albumArtUrl = "https://www.zapsplat.com/wp-content/uploads/2015/09/music_zapsplat_acoustic_folk.png",
                audioUrl = "https://www.zapsplat.com/wp-content/uploads/2015/09/music_zapsplat_acoustic_folk.mp3"
            )
            // Add more songs here
        )
    }

    private fun setupRecyclerView() {
        binding.rvSongList.layoutManager = LinearLayoutManager(this)
        // Pass a mutable list to the adapter
        songAdapter = SongAdapter(ArrayList(songList)) { clickedSong, position ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                // Pass the original index of the clicked song from the *unfiltered* list
                putExtra("current_song_index", songList.indexOf(clickedSong))
                putExtra("song_list", ArrayList(songList)) // Pass the entire original list
            }
            startActivity(intent)
        }
        binding.rvSongList.adapter = songAdapter
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this implementation
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                filterSongs(query)
                // Show/hide clear button based on text presence
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed for this implementation
            }
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear() // Clear the search text
        }
    }

    private fun filterSongs(query: String) {
        val filteredList = if (query.isBlank()) {
            songList // If query is empty, show all songs
        } else {
            songList.filter {
                it.title.contains(query, ignoreCase = true) || // Case-insensitive search by title
                        it.artist.contains(query, ignoreCase = true) // Case-insensitive search by artist
            }
        }
        songAdapter.updateList(filteredList) // Update the adapter with the filtered list
    }
}