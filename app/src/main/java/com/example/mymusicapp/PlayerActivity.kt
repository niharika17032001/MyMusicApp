package com.example.mymusicapp

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mymusicapp.model.Song

class PlayerActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: ImageButton
    private lateinit var stopButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var progressBar: SeekBar
    private lateinit var songTitleTextView: TextView
    private lateinit var albumArtImageView: ImageView

    private lateinit var songs: List<Song>
    private var currentPosition: Int = 0

    private val handler = Handler()
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playPauseButton = findViewById(R.id.playPauseButton)
        stopButton = findViewById(R.id.stopButton)
        nextButton = findViewById(R.id.nextButton)
        prevButton = findViewById(R.id.prevButton)
        progressBar = findViewById(R.id.progressBar)
        songTitleTextView = findViewById(R.id.songTitleTextView)
        albumArtImageView = findViewById(R.id.albumArtImageView)

        songs = intent.getParcelableArrayListExtra<Song>("songs") ?: emptyList()
        currentPosition = intent.getIntExtra("position", 0)

        mediaPlayer = MediaPlayer()

        playPauseButton.setOnClickListener {
            if (isPlaying) pauseMusic() else playMusic()
        }

        stopButton.setOnClickListener {
            stopMusic()
        }

        nextButton.setOnClickListener {
            nextSong()
        }

        prevButton.setOnClickListener {
            prevSong()
        }

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        playSong(songs[currentPosition])
    }

    private fun playSong(song: Song) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(song.url)
        mediaPlayer.prepareAsync()

        mediaPlayer.setOnPreparedListener {
            it.start()
            isPlaying = true
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause)

            songTitleTextView.text = song.title
            Glide.with(this).load(song.art).into(albumArtImageView)

            progressBar.max = mediaPlayer.duration
            updateProgress()
        }

        mediaPlayer.setOnCompletionListener {
            nextSong()
        }
    }

    private fun updateProgress() {
        progressBar.progress = mediaPlayer.currentPosition
        if (isPlaying) {
            handler.postDelayed({ updateProgress() }, 1000)
        }
    }

    private fun playMusic() {
        mediaPlayer.start()
        isPlaying = true
        playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
        updateProgress()
    }

    private fun pauseMusic() {
        mediaPlayer.pause()
        isPlaying = false
        playPauseButton.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun stopMusic() {
        mediaPlayer.pause()
        mediaPlayer.seekTo(0)
        isPlaying = false
        playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        progressBar.progress = 0
    }

    private fun nextSong() {
        currentPosition = (currentPosition + 1) % songs.size
        playSong(songs[currentPosition])
    }

    private fun prevSong() {
        currentPosition = if (currentPosition - 1 < 0) songs.size - 1 else currentPosition - 1
        playSong(songs[currentPosition])
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}
