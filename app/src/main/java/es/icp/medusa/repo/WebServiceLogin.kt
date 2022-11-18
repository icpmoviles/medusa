package es.icp.medusa.repo

import android.content.Context
import android.util.Base64
import com.android.volley.Request.Method.GET
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import es.icp.medusa.data.remote.modelos.request.LoginRequest
import es.icp.medusa.data.remote.modelos.response.AuthResponse
import es.icp.medusa.data.remote.modelos.response.UsuarioResponse
import es.icp.medusa.utils.Constantes.BASE_URL
import es.icp.medusa.utils.Constantes.ENDPOINT_ISTOKENVALID
import es.icp.medusa.utils.Constantes.ENDPOINT_LOGIN
import es.icp.medusa.utils.Constantes.ENDPOINT_LOGOUT
import es.icp.medusa.utils.Constantes.ENDPOINT_REFRESH_TOKEN
import es.icp.medusa.utils.Constantes.ENDPOINT_USERS
import es.icp.medusa.utils.toJson
@Deprecated("migracion a retrofit")
object WebServiceLogin {

    fun doLogin(context: Context, request: LoginRequest, respuesta: (AuthResponse?) -> Unit){
        val url = BASE_URL + ENDPOINT_LOGIN

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : JsonObjectRequest(
            POST,
            url,
            request.toJson(),
            Response.Listener{
                val authResponse = Gson().fromJson(it.toString(), AuthResponse::class.java)
                respuesta.invoke(authResponse)
                             },
            Response.ErrorListener { error ->
                respuesta.invoke(null)
                error.printStackTrace()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "ih" to "H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8="
                )
            }

        }

        requestQueue.add(stringRequest)
    }

    fun getUserDataFromServer(context: Context, token: String, respuesta: (UsuarioResponse?) -> Unit){
        val url = BASE_URL + ENDPOINT_USERS

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            GET,
            url,
            Response.Listener{
                val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
                val usuario = gson.fromJson(it, UsuarioResponse::class.java)
                respuesta.invoke(usuario)
                             },
            Response.ErrorListener { error ->
                respuesta.invoke(null)
                error.printStackTrace()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "ih" to "H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8=",
                    "Authorization" to "Bearer $token"
                )
            }
        }

        requestQueue.add(stringRequest)

    }

    fun isTokenValid(context: Context, token: String, respuesta: (Boolean)-> Unit){

        val url = BASE_URL + ENDPOINT_ISTOKENVALID

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            GET,
            url,
            Response.Listener{ respuesta.invoke(true) },
            Response.ErrorListener { error ->
                respuesta.invoke(false)
                error.printStackTrace()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "ih" to "H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8=",
                    "Authorization" to "Bearer $token"
                )
            }
        }

        requestQueue.add(stringRequest)
    }

    fun invalidateToken(context: Context,token: String, respuesta: (Boolean) -> Unit){
        val url = BASE_URL + ENDPOINT_LOGOUT

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            GET,
            url,
            Response.Listener{ respuesta.invoke(it.toBoolean()) },
            Response.ErrorListener { error ->
                respuesta.invoke(false)
                error.printStackTrace()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "ih" to "H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8=",
                    "Authorization" to "Bearer $token"
                )
            }
        }

        requestQueue.add(stringRequest)
    }

    fun refreshToken(context: Context,token: String, rfToken: String, respuesta: (AuthResponse?) -> Unit){
        val url = BASE_URL + ENDPOINT_REFRESH_TOKEN

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            POST,
            url,
            Response.Listener {
                val authResponse = Gson().fromJson(it, AuthResponse::class.java)
                respuesta.invoke(authResponse)
                             },
            Response.ErrorListener { error ->
                respuesta.invoke(null)
                error.printStackTrace()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "ih" to "H4sIAAAAAAACCqpW8kxxLChQslIwNK8FAAAA//8=",
                    "Authorization" to "Bearer $token"
                )
            }

            override fun getParams(): MutableMap<String, String>? {
                return mutableMapOf(
                    "icprt" to Base64.encodeToString(rfToken.toByteArray(), Base64.DEFAULT)
                )
            }
        }

        requestQueue.add(stringRequest)
    }
}