// File: PlayerActivity.kt
package com.example.mymusicapp

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mymusicapp.model.Song

class PlayerActivity : AppCompatActivity() {

    private lateinit var song: Song
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler()

    private lateinit var titleTextView: TextView
    private lateinit var artistTextView: TextView
    private lateinit var albumArtImageView: ImageView
    private lateinit var playPauseButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var seekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        song = intent.getParcelableExtra("song")!!

        titleTextView = findViewById(R.id.songTitleTextView)
        artistTextView = findViewById(R.id.artistTextView)
        albumArtImageView = findViewById(R.id.albumArtImageView)
        playPauseButton = findViewById(R.id.playPauseButton)
        nextButton = findViewById(R.id.nextButton)
        prevButton = findViewById(R.id.prevButton)
        seekBar = findViewById(R.id.seekBar)

        titleTextView.text = song.title
        artistTextView.text = song.artist
        Glide.with(this).load(song.art).into(albumArtImageView)

        playSong(song.url)

        playPauseButton.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            } else {
                mediaPlayer?.start()
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer?.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playSong(url: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                start()
                seekBar.max = duration
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                updateSeekBar()
            }
        }
    }

    private fun updateSeekBar() {
        mediaPlayer?.let {
            seekBar.progress = it.currentPosition
            if (it.isPlaying) handler.postDelayed({ updateSeekBar() }, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}
