package co.tiagoaguiar.netflixremake

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import co.tiagoaguiar.netflixremake.model.Movie
import co.tiagoaguiar.netflixremake.util.DowloandImageTask
import com.squareup.picasso.Picasso

//devemos usar o Adapter desta forma quando não desejarmos trabalhar com o adapter dentro activity
//ou caso tenhamos duas ou mais activitys que necessitamos utilizar o MainAdapter independente
//outro motivo é quando a sua activity está muito cheia de linhas de código
//Aqui é a lista horizontal
class MovieAdapter(
    private val movies: List<Movie>,
    @LayoutRes private val layoutid: Int,

    //function que ira ouvir o evento do touch -- irá pegar o id do filme
    private val onItemClickListener: ( (Int) -> Unit)? = null
    ) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutid, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(movie: Movie) {
            val imageCover: ImageView = itemView.findViewById(R.id.img_cover)
//
//            //ouvir o evento de clique na images
            imageCover.setOnClickListener {
                onItemClickListener?.invoke(movie.id)
            }

//            DowloandImageTask(object : DowloandImageTask.Callback {
//                override fun onResult(bitmap: Bitmap) {
//                    imageCover.setImageBitmap(bitmap)
//                }
//            }).execute(movie.coverUrl)

            //TODO: aqui será uma url que irá vir do servidor
           // imageCover.setImageResource(movie.coverUrl)
            Picasso.get().load(movie.coverUrl).into(imageCover) // biblioteca do picasso
        }

    }

}