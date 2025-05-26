package com.example.mymusicapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymusicapp.databinding.ActivityMainBinding
import com.example.mymusicapp.model.Song

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var songs: List<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        songs = listOf(
            Song("Aankhon feat. Ankur Tewari", "https://pagalworldmusic.com/download.php?title=Aankhon+feat.+Ankur+Tewari-320kbps&path=downloads%2Fhigh%2FGCMTcwNWWnc%2FGCMTcwNWWnc.mp3", "https://pagalworldmusic.com/downloads/cover/4572576/4572576.jpg", "Ankur Tewari"),
            Song("Kesariya", "https://pagalworld.com.se/files/download/id/64669", "https://pagalworld.com.se/siteuploads/thumb/sft2/64669_4.jpg", "Arijit Singh")
        )

        binding.songListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.songListRecyclerView.adapter = SongAdapter(songs) { song ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("song", song)
            startActivity(intent)
        }
    }
}
