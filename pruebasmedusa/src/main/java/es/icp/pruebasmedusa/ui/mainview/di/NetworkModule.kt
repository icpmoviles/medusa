package es.icp.pruebasmedusa.ui.mainview.di

import android.accounts.AccountManager
import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.icp.medusa.data.remote.service.AuthRetrofit
import es.icp.medusa.data.remote.service.AuthService
import es.icp.medusa.repositorio.AuthRepo
import es.icp.pruebasmedusa.ui.mainview.service.MockService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun getRetrofit(): Retrofit {

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
            .baseUrl("https://ticketingicp.icp.es:9013/").build()
    }

    @Provides
    @Singleton
    fun provideAuthRepo(accountManager: AccountManager): AuthRepo {
        return AuthRepo(
            authService = this.getRetrofit().create(),
            am = accountManager
        )
    }

    @Provides
    @Singleton
    fun provideAccountManager(context: Context): AccountManager {
        return AccountManager.get(context)
    }
}