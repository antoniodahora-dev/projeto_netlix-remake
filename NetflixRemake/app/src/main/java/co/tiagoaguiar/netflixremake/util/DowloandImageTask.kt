package co.tiagoaguiar.netflixremake.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import co.tiagoaguiar.netflixremake.model.Category
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class DowloandImageTask(private val callback: Callback){

    //será inserido no meio do Loop Central da UI-Tread
    private val handler = Handler(Looper.getMainLooper())

    //utilizando a UI-Thread(1)
    val executor = Executors.newSingleThreadExecutor()

    //irá retornar as informações para activity
    interface Callback {
        fun onResult(bitmap: Bitmap) // mapear os pixels da imagens
    }

    fun execute(url: String) {
        executor.execute {

            var urlConnection: HttpsURLConnection? = null
            var stream: InputStream? = null

            try {
                // UI-Thread(2) processo paralelo ao ui-thread (1)
                val requestURL = URL(url)// --> abrir uma URL

                urlConnection = requestURL.openConnection() as HttpsURLConnection //abrir a conexão com servidor
                urlConnection.readTimeout = 2000 // tempo de leitura que irá levar para busca a informação (2segundos)
                urlConnection.connectTimeout = 2000 // tempo de conexão com o app com o servidor (2segundos)

                val statusCode:Int = urlConnection.responseCode
                if (statusCode > 400) {
                    throw IOException("Erro na conexão com o servidor!")
                }

                stream = urlConnection.inputStream
                val bitmap = BitmapFactory.decodeStream(stream)
                handler.post {
                    callback.onResult(bitmap)
                }

            } catch (e: IOException) {
                val message = e.message?: "erro desconhecido"
                Log.e("Teste", message , e)

            } finally {
                //fechamos as conexoes
                urlConnection?.disconnect()
                stream?.close()
            }
        }
    }

}