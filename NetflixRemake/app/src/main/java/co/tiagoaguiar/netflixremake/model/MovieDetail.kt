package co.tiagoaguiar.netflixremake.model

data class MovieDetail(
    val movie: Movie,
    val similares: List<Movie>
)
