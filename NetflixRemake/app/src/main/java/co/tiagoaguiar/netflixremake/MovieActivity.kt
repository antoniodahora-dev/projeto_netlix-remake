package co.tiagoaguiar.netflixremake

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.RecoverySystem
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tiagoaguiar.netflixremake.model.Movie
import co.tiagoaguiar.netflixremake.model.MovieDetail
import co.tiagoaguiar.netflixremake.util.DowloandImageTask
import co.tiagoaguiar.netflixremake.util.MovieTask

class MovieActivity : AppCompatActivity(), MovieTask.Callback {

    private lateinit var txtTitle: TextView
    private lateinit var txtDesc: TextView
    private lateinit var txtCast: TextView
    private lateinit var adapter: MovieAdapter
    private lateinit var progress: ProgressBar
    private  val movies = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

       txtTitle = findViewById(R.id.movie_txt_title)
       txtDesc = findViewById(R.id.movie_txt_desc)
       txtCast = findViewById(R.id.movie_txt_cast)
       progress = findViewById(R.id.movie_progress)
       val rv: RecyclerView = findViewById(R.id.movie_rv_similar)

        val id = intent?.getIntExtra("id", 0) ?: throw IllegalStateException("ID não foi encontrado")

        val url = "https://api.tiagoaguiar.co/netflixapp/movie/$id?apiKey=02b65193-1eb9-4ecb-ac3e-5d8919a9d89e"

        MovieTask(this).execute(url)

        /*

        txtTitle.text = "Batman Begins"
        txtDesc.text = "Essa é a descrição do Filme"
        txtCast.text = getString(R.string.cast, "Ator A, Ator B, Ator C")
         */

        /*for(i in 0 until 15) {
            val movie = Movie(coverUrl = R.drawable.movie)
            movies.add(movie)
        }**/

        adapter = MovieAdapter(movies, R.layout.movie_item_similar)
        rv.layoutManager = GridLayoutManager(this, 3)
        //estamos passando o layout dos filmes da telas principal + o layout da tela similar
        rv.adapter = adapter

        //criar a toolbar através de código
        val toolbar: Toolbar = findViewById(R.id.toolbar_movie)
        setSupportActionBar(toolbar)

        //personalizar o icone
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        //ContextCompat - é para que todas as versões do android passa rebecer este desenhavél
//        val layerDrawable: LayerDrawable =  ContextCompat.getDrawable(this, R.drawable.shadows) as LayerDrawable
//
//        //busca o filme desejado
//        val movieCover = ContextCompat.getDrawable(this, R.drawable.movie_4)
//
//        //atribui a layer-list o novo filme
//        layerDrawable.setDrawableByLayerId(R.id.cover_drawable, movieCover)
//
//        //seta a imagem
//        val coverImg: ImageView = findViewById(R.id.movie_img)
//        coverImg.setImageDrawable(layerDrawable)

    }



    override fun onPreExecute() {
        progress.visibility = View.VISIBLE
    }

    override fun onResult(movieDetail: MovieDetail) {
        progress.visibility = View.GONE

        txtTitle.text = movieDetail.movie.title
        txtDesc.text = movieDetail.movie.desc
        txtCast.text = movieDetail.movie.cast

        movies.clear()
        movies.addAll(movieDetail.similares)
        adapter.notifyDataSetChanged()

        DowloandImageTask(object : DowloandImageTask.Callback {
            override fun onResult(bitmap: Bitmap) {
                val layerDrawable: LayerDrawable =  ContextCompat.getDrawable(
                    this@MovieActivity, R.drawable.shadows) as LayerDrawable
                val movieCover = BitmapDrawable(resources, bitmap)
                layerDrawable.setDrawableByLayerId(R.id.cover_drawable, movieCover)
                val coverImg: ImageView = findViewById(R.id.movie_img)
                coverImg.setImageDrawable(layerDrawable)
            }
        }).execute(movieDetail.movie.coverUrl)

    }

    override fun onFailure(message: String) {
        progress.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    //ativando o button de voltar do tootlbar no app
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //id padrão toolbar
        if (item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}