package co.tiagoaguiar.netflixremake.model

import androidx.annotation.DrawableRes

//data class Movie(@DrawableRes val coverUrl: Int)
data class Movie(
    val id: Int,
    val coverUrl: String,
    val title: String = "",
    val desc: String = "",
    val cast: String = ""
)