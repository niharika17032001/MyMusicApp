package com.example.mymusicapp // CHANGED HERE

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String,
    val audioUrl: String
) : Parcelable