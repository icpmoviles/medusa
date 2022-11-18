package es.icp.medusa.data.remote.service

import com.google.gson.GsonBuilder
import es.icp.medusa.data.remote.modelos.request.LoginRequest
import es.icp.medusa.data.remote.modelos.response.AuthResponse
import es.icp.medusa.data.remote.modelos.response.UsuarioResponse
import es.icp.medusa.utils.AuthEndPoints
import es.icp.medusa.utils.GlobalVariable
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface AuthService {


    companion object {

        private var retrofitService: AuthService? = null
        private const val ih = "H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8="

        fun getInstance(client: OkHttpClient) : AuthService {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(AuthEndPoints.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()))
                    .client(client)
                    .build()
                retrofitService = retrofit.create(AuthService::class.java)
            }
            return retrofitService!!
        }
    }


    @POST
    suspend fun doLogin (
        @Url url: String,
        @Body request: LoginRequest
    ) : Response<AuthResponse>


    @GET
    suspend fun getUserDataFromServer(
        @Url url: String
    ): Response<UsuarioResponse>

    @GET
    suspend fun isTokenValid(
        @Url url: String
    ): Response<*>

    @GET
    suspend fun logOut(
        @Url url: String
    ): Response<Void>


    @POST
    @FormUrlEncoded
    suspend fun refreshAuthToken(
        @Url url: String,
        @Field("icprt") icprt: String
    ): Response<AuthResponse>
}
