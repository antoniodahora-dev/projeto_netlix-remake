package co.tiagoaguiar.netflixremake.util

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import co.tiagoaguiar.netflixremake.model.Category
import co.tiagoaguiar.netflixremake.model.Movie
import co.tiagoaguiar.netflixremake.model.MovieDetail
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class MovieTask(private val callback: Callback) {

    //será inserido no meio do Loop Central da UI-Tread
    private val handler = Handler(Looper.getMainLooper())

    //utilizando a UI-Thread(1)
    val executor = Executors.newSingleThreadExecutor()



    //irá retornar as informações para activity
    interface Callback {

        //antes de comecar a execução será chamada essa função
        fun onPreExecute()

        fun onResult(movieDetail: MovieDetail)

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

                //erro 400 - mensagem será enviada ao usuario
                if (statusCode == 400) {
                    stream = urlConnection.errorStream
                    buffer = BufferedInputStream(stream)
                    val jsonAsString = toString(buffer)

                    val json = JSONObject(jsonAsString)
                    val message = json.getString("message")

                    throw IOException(message)

                } else if (statusCode > 400) {
                    throw IOException("Erro na conexão com o servidor!")
                }

                stream = urlConnection.inputStream // stream é uma sequencia de bytes
                buffer = BufferedInputStream(stream)
                val jsonAsString = toString(buffer)

                //o JSON esta preparado para ser convertido em um DATA CLASS
                val movieDetail = toMovieDetail(jsonAsString)

                handler.post {
                    // interface Callback
                    callback.onResult(movieDetail)
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

    //vamos retornar uma lista de filmes
    private fun toMovieDetail(jsonAsString: String) : MovieDetail {
        val json = JSONObject(jsonAsString)

        val id = json.getInt("id")
        val title = json.getString("title")
        val desc = json.getString("desc")
        val cast = json.getString("cast")
        val coverUrl = json.getString("cover_url")
        val jsonMovies = json.getJSONArray("movie")

        val similiars = mutableListOf<Movie>()
        for (i in 0 until jsonMovies.length()) {
            val jsonMovie = jsonMovies.getJSONObject(i)

            val similarId = jsonMovie.getInt("id")
            val similarCoverUrl = jsonMovie.getString("cover_url")

            val m = Movie(similarId, similarCoverUrl)
            similiars.add(m)
        }

        val movie = Movie(id, coverUrl, desc, title, cast)
        return MovieDetail(movie, similiars)

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