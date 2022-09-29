package es.icp.medusa.repo

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Request.Method.GET
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import es.icp.icp_commons.CheckRequest
import es.icp.icp_commons.Interfaces.NewVolleyCallBack
import es.icp.icp_commons.Objects.ParametrosPeticion
import es.icp.medusa.modelo.TokenRequest
import es.icp.medusa.modelo.TokenResponse
import es.icp.medusa.modelo.UsuarioLogin
import es.icp.medusa.repo.interfaces.RepoResponse
import es.icp.medusa.repo.interfaces.Ws_Callback
import es.icp.medusa.utils.Constantes
import es.icp.medusa.utils.Constantes.BASE_URL
import es.icp.medusa.utils.Constantes.ENDPOINT_ISTOKENVALID
import es.icp.medusa.utils.Constantes.ENDPOINT_LOGIN
import es.icp.medusa.utils.Constantes.ENDPOINT_LOGOUT
import es.icp.medusa.utils.Constantes.ENDPOINT_REFRESH_TOKEN
import es.icp.medusa.utils.Constantes.ENDPOINT_USERS
import es.icp.medusa.utils.Dx
import org.json.JSONObject

object WebServiceLogin {



    fun doLogin(context: Context, request: TokenRequest, repoResponse: RepoResponse){
        val url = BASE_URL + ENDPOINT_LOGIN

        procesarRequest(
            context,
            request.toJson(),
            url,
            true,
            Request.Method.POST,
            TokenResponse(),
            object : Ws_Callback {
                override fun online(response: Any) {
                    repoResponse.respuesta(response)
                }
                override fun offline() {
                    Dx.dxSinConexion(context){}
                }
            },
            false
        )

    }

    fun getUserDataFromServer(context: Context, token: String, repoResponse: RepoResponse){
        val url = BASE_URL + ENDPOINT_USERS

        procesarRequestConHeaders(
            context,
            JSONObject(),
            url,
            true,
            GET,
            null,
            token,
            object : Ws_Callback {
                override fun online(response: Any) {
                    val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").serializeNulls().create()
                    val userLogin = gson.fromJson(response.toString(), UsuarioLogin::class.java)
                    repoResponse.respuesta(userLogin)
                }

                override fun offline() {
                    Dx.dxSinConexion(context){}
                }
            },
            false
        )

    }

    fun isTokenValid(context: Context, token: String, respuesta: (Boolean)-> Unit){

        val url = BASE_URL + ENDPOINT_ISTOKENVALID

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
                    "ih" to "H4sIAAAAAAACCqpW8kxxLChQsjLXAbKccxIzc0MqC1KLlazySnNyagEAAAD//w==",
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
                    "ih" to "H4sIAAAAAAACCqpW8kxxLChQsjLXAbKccxIzc0MqC1KLlazySnNyagEAAAD//w==",
                    "Authorization" to "Bearer $token"
                )
            }
        }

        requestQueue.add(stringRequest)
    }

    fun refreshToken(context: Context,token: String, rfToken: String, respuesta: (String?) -> Unit){
        val url = BASE_URL + ENDPOINT_REFRESH_TOKEN

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            POST,
            url,
            Response.Listener {
                respuesta.invoke(it)
                             },
            Response.ErrorListener { error ->
                respuesta.invoke(null)
                error.printStackTrace()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "ih" to "H4sIAAAAAAACCqpW8kxxLChQsjLXAbKccxIzc0MqC1KLlazySnNyagEAAAD//w==",
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

    fun procesarRequest(
        context: Context,
        request: JSONObject,
        url: String,
        loader: Boolean,
        method: Int,
        clase : Any?, // esto es para castear al response que corresponda hay que añadirlo siempre
        callback: Ws_Callback,
        guardarAccion : Boolean = false
    ) {
        val parametros = ParametrosPeticion()
        if (clase != null) {
            parametros.clase = clase.javaClass
        }
        parametros.url = url
        if (method == Request.Method.POST) {
            parametros.setMethod(ParametrosPeticion.Method.POST)
        } else {
            parametros.setMethod(ParametrosPeticion.Method.GET)
        }
        parametros.jsonObject = request
        Log.w("myapp URL",url)

        try {
            CheckRequest.CheckAndSend(context, parametros, object : NewVolleyCallBack {
                override fun onSuccess(result: Any) {
                    Log.w("myapp","JSON RESULT: $result")
                    callback.online(result)
                }

                override fun onError(error: VolleyError?) {
                    if (error?.networkResponse?.statusCode == 401){
                        Dx.dxWebServiceError(context, "Error","Usuario o contraseña incorrectas."){}
                    }
                }

                override fun onOffline() {
                    callback.offline()
                }
            }, loader, /*"ID_USUARIO"*/1, "", guardarAccion)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun procesarRequestConHeaders(
        context: Context,
        request: JSONObject,
        url: String,
        loader: Boolean,
        method: Int,
        clase: Any?,
        token : String,
        callback: Ws_Callback,
        guardarAccion : Boolean = false
    ) {

        //val json = JSONObject(Gson().toJson(request))
        val parametros = ParametrosPeticion()

        val headers : Map<String, String> = mapOf(
            "ih" to "H4sIAAAAAAACCqpW8kxxLChQsjLXAbKccxIzc0MqC1KLlazySnNyagEAAAD//w==",
            "Authorization" to "Bearer $token"
           /* , "Content-Type" to "application/json; charset=UTF-8"*/)
//        val headers : Map<String, String> = mapOf("authorization" to token)

        Log.w("header", headers.toString())

        parametros.url = url
        if (method == POST) {
            parametros.setMethod(ParametrosPeticion.Method.POST)
        } else {
            parametros.setMethod(ParametrosPeticion.Method.GET)
        }
        if (clase != null)
            parametros.clase = clase.javaClass

        parametros.jsonObject = request


        try {
            CheckRequest.CheckAndSend(context, parametros, object : NewVolleyCallBack {
                override fun onSuccess(result: Any?) {
                    Log.w("JSON RESULT: ", "$result")
                    callback.online(result!!)
                }

                override fun onError(error: VolleyError?) {
                    when (error?.networkResponse?.statusCode){
                        401 -> Dx.dxWebServiceError(context, "Error", error.message ?: "No autorizado"){}
                        else -> Dx.dxWebServiceError(context, "Error", error?.message?: "Se ha producido un error de comunicación con el servidor."){}
                    }

                }

                override fun onOffline() {
                    callback.offline()
                }
            }, loader, 0, "", guardarAccion, headers)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}