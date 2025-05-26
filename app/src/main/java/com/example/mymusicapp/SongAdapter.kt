package com.example.mymusicapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mymusicapp.databinding.ItemSongBinding
import com.example.mymusicapp.model.Song

class SongAdapter(private val songs: List<Song>, val onClick: (Song) -> Unit) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.binding.songTitleText.text = song.title
        holder.binding.songArtistText.text = song.artist
        Glide.with(holder.itemView).load(song.art).into(holder.binding.songArtImage)

        holder.itemView.setOnClickListener { onClick(song) }
    }

    override fun getItemCount(): Int = songs.size
}
