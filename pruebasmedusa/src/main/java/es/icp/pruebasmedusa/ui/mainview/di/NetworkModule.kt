package es.icp.pruebasmedusa.ui.mainview.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.icp.genericretrofit.communication.ConnectivityInterceptor
import es.icp.genericretrofit.communication.RetrofitBase
import es.icp.pruebasmedusa.ui.mainview.service.MockService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Esto no tiene nada que ver con un Singleton, estoy indicando que todos los inject que declare aqui serán a nivel da aplicacion.
class NetworkModule {

    @Singleton // Ahora si, especifico que solo me cree una instancia a nivel de la aplicación
    @Provides
    fun provideRetrofit(context: Context): Retrofit {
        // TODO BUILD VARIANTS
        return RetrofitBase.getInstance(
            baseUrl = "https://ticketingicp.icp.es:9013/",
            client = OkHttpClient.Builder().apply {
                interceptors().add(ConnectivityInterceptor(context))
                readTimeout(1, TimeUnit.MINUTES)
                writeTimeout(1, TimeUnit.MINUTES)
                connectTimeout(1, TimeUnit.MINUTES)
            }.build()
        )
    }

    @Singleton
    @Provides
    fun provideRecepcionesService(retrofit: Retrofit): MockService {
        return retrofit.create(MockService::class.java)
    }

}