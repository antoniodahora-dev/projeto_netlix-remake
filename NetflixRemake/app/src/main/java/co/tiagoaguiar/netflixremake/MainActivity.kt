package co.tiagoaguiar.netflixremake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tiagoaguiar.netflixremake.model.Category
import co.tiagoaguiar.netflixremake.model.Movie
import co.tiagoaguiar.netflixremake.util.CategoryTask

class MainActivity : AppCompatActivity(), CategoryTask.Callback {

    //variavel do progressBar
    private lateinit var progress: ProgressBar

    //ficara visivel para as demais functions
    val categories = mutableListOf<Category>()
    private lateinit var adapter: CategoryAdapter

    //modelo mvc
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("Teste", "onCreate")

        progress = findViewById(R.id.progress_main)


        //ira criar a lista de filmes e ira joga dentro da lista de categoria
        //loop de categorias
       /* for (j in 0 until 10) {

            val movies = mutableListOf<Movie>()

            //irá pegar todos os filmes da categoria
            //loop de filmes
            for(i in 0 until 7) {
                val movie = Movie(coverUrl = R.drawable.movie)
                movies.add(movie)
            }
            // irá pegar o filmes e colocar dentro das categorias
            val category = Category("cat $j", movies)
            categories.add(category)

        }*/


        //na vertical teremos a Lista (CategoryAdapter) de categorias
        //dentro de cada item [TextView + RecyclerView horizontal]
        // uma lista (MovieAdapter) de filmes (ImageView)
        adapter = CategoryAdapter(categories){ id ->
            val intent = Intent(this@MainActivity, MovieActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)

        }
        val rv: RecyclerView = findViewById(R.id.rv_main)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        CategoryTask(this)
            .execute("https://api.tiagoaguiar.co/netflixapp/home?apiKey=02b65193-1eb9-4ecb-ac3e-5d8919a9d89e")

    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE // exibe o progressBar
    }

    override fun onResult(categories: List<Category>) {
       //aqui será quando o CategoryTask irá retornar o Callback

        //iremos limpar a lista de categorias
        this.categories.clear()

        //agora vamos adicionar as categorias -- api
        this.categories.addAll(categories)

        //o adapter será notificado que as informações foram modificadas (categorias)
        //forca o adapter para redesenhar as categorias
        this.adapter.notifyDataSetChanged()

        Log.i("Teste Activity", categories.toString())
        Toast.makeText(this, "Informações sendo carregadas!", Toast.LENGTH_LONG).show()
        progress.visibility = View.GONE // retira o progressBar da tela
    }

    //irá exibir mensagem de erro
    override fun onFailure(message: String) {

        AlertDialog.Builder(this)
            .setTitle("Ops!")
            .setMessage( message + "\nTenta novamente mais tarde!" )
            .setPositiveButton(android.R.string.ok)
            {dialog, which ->

                finish() //fechará o app
            }
            .create()
            .show()

        //Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}