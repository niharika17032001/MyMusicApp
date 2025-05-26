//package com.example.mymusicapp
//
//import android.media.MediaPlayer
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import com.bumptech.glide.Glide
//import com.example.mymusicapp.model.Song
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import okhttp3.*
//import java.io.IOException
//
//class `MainActivity-1` : AppCompatActivity() {
//    private lateinit var songs: List<Song>
//    private var currentIndex = 0
//    private var mediaPlayer: MediaPlayer? = null
//    private val client = OkHttpClient()
//    private val gson = Gson()
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_player)
//
//        // 🎵 Hardcoded demo song list
//        songs = listOf(
//            Song(
//                title = "Aankhon feat. Ankur Tewari",
//                url = "https://pagalworldmusic.com/download.php?title=Aankhon+feat.+Ankur+Tewari-320kbps&path=downloads%2Fhigh%2FGCMTcwNWWnc%2FGCMTcwNWWnc.mp3",
//                art = "https://pagalworldmusic.com/downloads/cover/4572576/4572576.jpg"
//            ), Song(
//                title = "Aankhon feat. Ankur Tewari",
//                url = "https://pagalworldmusic.com/download.php?title=Aankhon+feat.+Ankur+Tewari-320kbps&path=downloads%2Fhigh%2FGCMTcwNWWnc%2FGCMTcwNWWnc.mp3",
//                art = "https://pagalworldmusic.com/downloads/cover/4572576/4572576.jpg"
//            ),
//            Song(
//                title = "Tu Hai Kahan",
//                url = "https://pagalworld.com.se/files/download/id/71215",
//                art = "https://pagalworld.com.se/siteuploads/thumb/sft3/71215_4.jpg"
//            ),
//            Song(
//                title = "Raatan Lambiyan",
//                url = "https://pagalworld.com.se/files/download/id/65089",
//                art = "https://pagalworld.com.se/siteuploads/thumb/sft3/65089_4.jpg"
//            )
//        )
//
//
//        playSong(songs[currentIndex])
//
//        findViewById<Button>(R.id.playButton).setOnClickListener {
//            mediaPlayer?.start()
//        }
//
//        findViewById<Button>(R.id.stopButton).setOnClickListener {
//            mediaPlayer?.pause()
//        }
//
//        findViewById<Button>(R.id.nextButton).setOnClickListener {
//            currentIndex = (currentIndex + 1) % songs.size
//            playSong(songs[currentIndex])
//        }
//
//        findViewById<Button>(R.id.shuffleButton).setOnClickListener {
//            songs = songs.shuffled()
//            currentIndex = 0
//            playSong(songs[currentIndex])
//        }
//    }
//
//
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_player)
////
////        fetchSongsFromUrl("https://drive.google.com/uc?export=download&id=1GoFmRDNyu9Y9jAUoJOIqKYob2HucG3UX") { fetchedSongs ->
////            if (fetchedSongs.isNotEmpty()) {
////                songs = fetchedSongs
////                playSong(songs[currentIndex])
////            } else {
////                Log.e("MainActivity", "No songs found")
////            }
////        }
////
////        findViewById<Button>(R.id.playButton).setOnClickListener {
////            mediaPlayer?.start()
////        }
////
////        findViewById<Button>(R.id.stopButton).setOnClickListener {
////            mediaPlayer?.pause()
////        }
////
////        findViewById<Button>(R.id.nextButton).setOnClickListener {
////            if (::songs.isInitialized && songs.isNotEmpty()) {
////                currentIndex = (currentIndex + 1) % songs.size
////                playSong(songs[currentIndex])
////            }
////        }
////
////        findViewById<Button>(R.id.shuffleButton).setOnClickListener {
////            if (::songs.isInitialized && songs.isNotEmpty()) {
////                songs = songs.shuffled()
////                currentIndex = 0
////                playSong(songs[currentIndex])
////            }
////        }
////    }
//
//    private fun playSong(song: Song) {
//        mediaPlayer?.release()
//        mediaPlayer = MediaPlayer().apply {
//            setDataSource(song.url)
//            setOnPreparedListener { start() }
//            prepareAsync()
//        }
//
//        findViewById<TextView>(R.id.songTitleTextView).text = song.title
//        Glide.with(this)
//            .load(song.art)
////            .placeholder(R.drawable.placeholder_image)  // optional placeholder drawable
////            .error(R.drawable.error_image)  // optional error drawable
//            .into(findViewById<ImageView>(R.id.albumArtImageView))
//    }
//
//    private fun fetchSongsFromUrl(url: String, onLoaded: (List<Song>) -> Unit) {
//        val request = Request.Builder().url(url).build()
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("MainActivity", "Failed to fetch songs: ${e.message}")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val json = response.body?.string()
//                if (json != null) {
//                    val type = object : TypeToken<List<Song>>() {}.type
//                    val songs = gson.fromJson<List<Song>>(json, type)
//                    runOnUiThread { onLoaded(songs) }
//                } else {
//                    Log.e("MainActivity", "Response body null")
//                }
//            }
//        })
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mediaPlayer?.release()
//        mediaPlayer = null
//    }
//}
