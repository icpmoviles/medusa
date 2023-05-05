package es.icp.medusa.data.remote.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import es.icp.genericretrofit.communication.NetworkResponseAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AuthRetrofit {

    var retrofit: Retrofit? = null

    private val mGson: Gson =
        GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .serializeNulls()
            .setPrettyPrinting()
            .create()

    fun getInstance(
        baseUrl: String,
        client: OkHttpClient? = null,
        gson: Gson = mGson
    ) : Retrofit {
        val builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))


        return retrofit ?: kotlin.run {
            retrofit = client?.let {
                builder.client(it).build()
            } ?: kotlin.run {
                builder.build()
            }
            retrofit!!
        }
    }

}