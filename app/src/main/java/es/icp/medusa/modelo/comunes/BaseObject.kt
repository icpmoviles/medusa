package es.icp.medusa.modelo.comunes

import com.google.gson.Gson
import org.json.JSONObject

open class BaseObject {

    fun toJson() : JSONObject {
        return JSONObject(Gson().toJson(this).toString())
    }

    override fun toString(): String {
        return Gson().toJson(this).toString()
    }
}