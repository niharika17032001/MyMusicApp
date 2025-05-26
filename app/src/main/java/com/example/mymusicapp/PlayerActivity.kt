package com.example.mymusicapp // CHANGED HERE

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mymusicapp.databinding.ActivityPlayerBinding // CHANGED HERE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var songList: ArrayList<Song>
    private var currentSongIndex: Int = -1
    private var playbackJob: Job? = null // Job for updating SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from Intent
        currentSongIndex = intent.getIntExtra("current_song_index", 0)
        songList = intent.getParcelableArrayListExtra("song_list") ?: arrayListOf()

        if (songList.isEmpty()) {
            finish() // Close activity if no songs are found
            return
        }

        setupListeners()
        playSong(currentSongIndex)
    }

    private fun setupListeners() {
        binding.btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        binding.btnNext.setOnClickListener {
            playNextSong()
        }

        binding.btnPrevious.setOnClickListener {
            playPreviousSong()
        }

        binding.sbPlaybackProgress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Pause updates while user is dragging
                playbackJob?.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Resume updates after user releases
                startPlaybackUpdates()
            }
        })
    }

    private fun playSong(index: Int) {
        if (index < 0 || index >= songList.size) {
            return // Prevent out of bounds
        }

        currentSongIndex = index
        val song = songList[currentSongIndex]

        // Release previous MediaPlayer instance if exists
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null // Ensure it's null before creating a new one

        // Update UI
        binding.tvPlayerSongTitle.text = song.title
        binding.tvPlayerArtistName.text = song.artist
        Glide.with(this)
            .load(song.albumArtUrl)
            .placeholder(android.R.drawable.sym_def_app_icon)
            .error(android.R.drawable.sym_def_app_icon)
            .into(binding.ivPlayerAlbumArt)

        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.audioUrl)
            prepareAsync() // Prepare asynchronously for network streams
            setOnPreparedListener { mp ->
                mp.start()
                binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                binding.sbPlaybackProgress.max = mp.duration
                binding.tvTotalTime.text = formatTime(mp.duration)
                startPlaybackUpdates() // Start updating progress bar
            }
            setOnCompletionListener {
                playNextSong() // Loop through playlist
            }
            setOnErrorListener { mp, what, extra ->
                // Handle errors, e.g., show a Toast
                mp.reset() // Reset MediaPlayer to idle state
                false // Return false to indicate the error was not handled
            }
        }
    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                playbackJob?.cancel() // Stop updates when paused
            } else {
                it.start()
                binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                startPlaybackUpdates() // Resume updates when playing
            }
        }
    }

    private fun playNextSong() {
        currentSongIndex = (currentSongIndex + 1) % songList.size
        playSong(currentSongIndex)
    }

    private fun playPreviousSong() {
        currentSongIndex = (currentSongIndex - 1 + songList.size) % songList.size
        playSong(currentSongIndex)
    }

    private fun startPlaybackUpdates() {
        playbackJob?.cancel() // Cancel any existing job
        playbackJob = CoroutineScope(Dispatchers.Main).launch {
            while (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                val currentPosition = mediaPlayer!!.currentPosition
                binding.sbPlaybackProgress.progress = currentPosition
                binding.tvCurrentTime.text = formatTime(currentPosition)
                delay(1000) // Update every second
            }
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        playbackJob?.cancel() // Cancel coroutine job
        mediaPlayer?.release() // Release MediaPlayer resources
        mediaPlayer = null
    }

    override fun onStop() {
        super.onStop()
        // If you want the music to stop when the app is in the background, uncomment this:
        // mediaPlayer?.pause()
        // binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
        // playbackJob?.cancel()
    }
}