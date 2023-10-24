package es.icp.medusa.di

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.icp.medusa.BuildConfig
import es.icp.medusa.data.remote.service.AuthService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {

    @Provides
    @Singleton
    fun getRetrofit(context: Context): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level =
                HttpLoggingInterceptor.Level.BODY // Puedes cambiar el nivel de logging según tus necesidades
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BuildConfig.BASE_URL).build()
    }

    @Provides
    @Singleton
    fun getAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

}