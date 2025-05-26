package com.example.mymusicapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val title: String,
    val url: String,
    val art: String,
    val artist: String
) : Parcelable
