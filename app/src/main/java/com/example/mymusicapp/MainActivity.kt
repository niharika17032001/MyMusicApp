package com.example.mymusicapp // CHANGED HERE

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymusicapp.databinding.ActivityMainBinding // CHANGED HERE

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var songList: List<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSongList()
        setupRecyclerView()
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
        val adapter = SongAdapter(songList) { clickedSong, position ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("current_song_index", position)
                putExtra("song_list", ArrayList(songList)) // Pass the entire list
            }
            startActivity(intent)
        }
        binding.rvSongList.adapter = adapter
    }
}