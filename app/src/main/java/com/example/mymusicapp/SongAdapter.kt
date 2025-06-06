package com.example.mymusicapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mymusicapp.databinding.ItemSongBinding

class SongAdapter(
    private var songs: ArrayList<Song>,
    private val onItemClick: (Song, Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song, position: Int) {
            binding.tvSongTitle.text = song.title
            binding.tvArtistName.text = song.artist

            Glide.with(binding.ivAlbumArt.context)
                .load(song.albumArtUrl)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .error(android.R.drawable.sym_def_app_icon)
                .into(binding.ivAlbumArt)

            binding.root.setOnClickListener {
                onItemClick(song, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position], position)
    }

    override fun getItemCount(): Int = songs.size

    fun addSongs(newSongs: List<Song>) {
        val startPosition = songs.size
        songs.addAll(newSongs)
        notifyItemRangeInserted(startPosition, newSongs.size)
    }

    fun updateList(newList: List<Song>) {
        songs.clear()
        songs.addAll(newList)
        notifyDataSetChanged()
    }
}