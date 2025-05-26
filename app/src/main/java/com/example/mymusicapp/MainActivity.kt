package com.example.mymusicapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapp.model.Song

class MainActivity : AppCompatActivity() {

    private val songs = listOf(
        Song(
            "Aankhon feat. Ankur Tewari",
            "https://pagalworldmusic.com/download.php?title=Aankhon+feat.+Ankur+Tewari-320kbps&path=downloads%2Fhigh%2FGCMTcwNWWnc%2FGCMTcwNWWnc.mp3",
            "https://pagalworldmusic.com/downloads/cover/4572576/4572576.jpg"
        ),
        Song(
            "Kesariya",
            "https://pagalworld.com.se/files/download/id/64669",
            "https://pagalworld.com.se/siteuploads/thumb/sft2/64669_4.jpg"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.songRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SongAdapter(songs) { song, position ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putParcelableArrayListExtra("songs", ArrayList(songs))
            intent.putExtra("position", position)
            startActivity(intent)
        }
    }
}
