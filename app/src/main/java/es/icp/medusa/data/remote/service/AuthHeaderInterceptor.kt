package es.icp.medusa.data.remote.service

import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor (private val token: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
//            .addHeader(
//                "Content-type:", "application/json; charset=UTF-8"
//            )
//            .addHeader("Accept", "*/*")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("ih", "H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8=")
            .build()

        return chain.proceed(request)
    }
}