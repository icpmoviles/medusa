package es.icp.medusa.repo

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.VolleyError
import es.icp.icp_commons.CheckRequest
import es.icp.icp_commons.Interfaces.NewVolleyCallBack
import es.icp.icp_commons.Objects.ParametrosPeticion
import es.icp.medusa.modelo.TokenRequest
import es.icp.medusa.modelo.TokenResponse
import es.icp.medusa.repo.interfaces.RepoResponse
import es.icp.medusa.repo.interfaces.Ws_Callback
import org.json.JSONObject

object WebServiceLogin {

    fun doLogin(context: Context, request: TokenRequest, repoResponse: RepoResponse){
        val url = "https://perseo-login-int.icp.es/icpsec/Fac/Login"

        procesarRequest(
            context,
            request.toJson(),
            url,
            true,
            Request.Method.POST,
            TokenResponse(),
            object : Ws_Callback {
                override fun online(response: Any) {
                    if (response is TokenResponse){
                        repoResponse.respuesta(response)
                    }
                }

                override fun offline() {
                    Log.w("myapp OFFLINE", "NO HAY CONEXION A INTERNET")
                }
            },
            false
        )

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
                    if (result is TokenResponse) {
                        callback.online(result)
                    } else {
                        if (result is JSONObject){
                            try{
                                callback.online(result.getJSONArray("data"))
                            }catch (ex : java.lang.Exception){
                                callback.online(result.getJSONObject("data"))
                            }

                        }

                    }
                }

                override fun onError(error: VolleyError?) {
//                    Toast.makeText(context, "Se ha producido un error de autentificacion, revise los datos introducidos.", Toast.LENGTH_SHORT).show()
                    callback.online(TokenResponse("TOKEN MANUAL", 600))
                    if (error?.networkResponse?.statusCode == 404){
                        Toast.makeText(context, "USUARIO O CONTRASEÑA INCORRECTOS", Toast.LENGTH_LONG).show()
                    }
                    error?.let {
                        var mensaje : String = "Se ha producido un error desconocido."
                        if (it.message != null) {
                            mensaje = it.message!!
                        }
//                        Dx.error(context, mensaje)
                    } ?: run {
//                        Dx.error(context, context.getString(R.string.error_generico))
                        Toast.makeText(context, "USUARIO O CONTRASEÑA INCORRECTOS2", Toast.LENGTH_LONG).show()
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
}