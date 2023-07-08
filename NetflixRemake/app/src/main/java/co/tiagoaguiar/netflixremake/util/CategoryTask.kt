package co.tiagoaguiar.netflixremake.util

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import co.tiagoaguiar.netflixremake.model.Category
import co.tiagoaguiar.netflixremake.model.Movie
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class CategoryTask(private val callback: Callback) {

    //será inserido no meio do Loop Central da UI-Tread
    private val handler = Handler(Looper.getMainLooper())

    //utilizando a UI-Thread(1)
    val executor = Executors.newSingleThreadExecutor()



    //irá retornar as informações para activity
    interface Callback {

        //antes de comecar a execução será chamada essa função
        fun onPreExecute()

        fun onResult(categories: List<Category>)

        //quando estiver uma falha será apresentado uma msg
        fun onFailure(message: String)
    }

    fun execute(url: String) {

        callback.onPreExecute()

        executor.execute {

            //para fechar a conexao apos ao acesso ao servicos
            var urlConnection: HttpsURLConnection? = null
            var buffer: BufferedInputStream? = null
            var stream: InputStream? = null

            try {
                // UI-Thread(2) processo paralelo ao ui-thread (1)
                val requestURL = URL(url)// --> abrir uma URL

                urlConnection = requestURL.openConnection() as HttpsURLConnection //abrir a conexão com servidor
                urlConnection.readTimeout = 2000 // tempo de leitura que irá levar para busca a informação (2segundos)
                urlConnection.connectTimeout = 2000 // tempo de conexão com o app com o servidor (2segundos)

                val statusCode:Int = urlConnection.responseCode // ira buscar as informaçoes do servidor

                if (statusCode > 400) {
                    throw IOException("Erro na conexão com o servidor!")
                }
                // 1ª forma de pegar a string do servidor
//                val stream = urlConnection.inputStream // stream é uma sequencia de bytes
//                val jsonAsString = stream.bufferedReader().use { it.readText() } // espaco de memoria que será armazenado no buffer
//                Log.i("teste", jsonAsString)

                // 2ªforma de pegar a string do servidor
                stream = urlConnection.inputStream // stream é uma sequencia de bytes
                buffer = BufferedInputStream(stream)
                val jsonAsString = toString(buffer)

                //o JSON esta preparado para ser convertido em um DATA CLASS
                val categories = toCategories(jsonAsString)

                handler.post {
                    // interface Callback
                    callback.onResult(categories)
                }

            } catch (e: IOException) {
                val message = e.message?: "erro desconhecido"
                Log.e("Teste", message , e)

                handler.post {
                    callback.onFailure(message)
                }

            } finally {
                //fechamos as conexoes
                urlConnection?.disconnect()
                buffer?.close()
                stream?.close()

            }
        }
    }

    //vamos retornar uma lista de categorias
    private fun toCategories(jsonAsString: String) : List<Category> {
        val categories = mutableListOf<Category>()

        //pegamos objeto raiz
        val jsonRoot = JSONObject(jsonAsString)

        //buscar a lista de categorias
        val jsonCategories = jsonRoot.getJSONArray("category")
        for (i in 0 until jsonCategories.length()) {
            val jsonCategory = jsonCategories.getJSONObject(i)

            val title = jsonCategory.getString("title")

            //pegando a lista de filmes
            val  jsonMovies = jsonCategory.getJSONArray("movie")

            // gerando a lista de filme
            val movies = mutableListOf<Movie>()
            for (j in 0 until jsonMovies.length()) {
                val jsonMovie = jsonMovies.getJSONObject(j)

                val id = jsonMovie.getInt("id")
                val coverUrl = jsonMovie.getString("cover_url")

                //pegando o filmes e adiciona a lista criada
                movies.add(Movie(id, coverUrl))
            }

            //adiciona a lista de titulos das categorias + a lista de filmes
            categories.add(Category(title, movies))
        }
        //retorna a lista de categoria
        return categories
    }

    //irá buscar todos os bytes
    private fun toString(stream: BufferedInputStream): String {
        val bytes = ByteArray(1024)
        val baos =  ByteArrayOutputStream()
        var read: Int

        while (true) {
            read = stream.read(bytes)
            if (read <= 0) {
                break
            }

            baos.write(bytes, 0 , read)
        }
        return String (baos.toByteArray())
    }
}