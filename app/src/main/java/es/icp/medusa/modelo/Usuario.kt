package es.icp.medusa.modelo

import com.google.gson.annotations.SerializedName
import es.icp.medusa.modelo.comunes.BaseObject
import java.io.Serializable
import java.util.*

data class Usuario(
    @SerializedName("idUsuario")
    var idUsuario: Int = 0,
    @SerializedName("usuario")
    var usuario: String = "",
    @SerializedName("idTrabajador")
    var idTrabajador: Int = 0,
    @SerializedName("nombreCompleto")
    var nombreCompleto: String = "",
    @SerializedName("nombre")
    var nombre: String = "",
    @SerializedName("apellido1")
    var apellido1: String = "",
    @SerializedName("apellido2")
    var apellido2: String = "",
    @SerializedName("numDocumento")
    var numDocumento: String = "",
    @SerializedName("idEmpresa")
    var idEmpresa: Int = 0,
    @SerializedName("email")
    var email: String = "",
    @SerializedName("fAlta")
    var fAlta: String = "",
    @SerializedName("fBaja")
    var fBaja: Date = Date(),
    @SerializedName("idPerfil")
    var idPerfil: Int = 0

) : BaseObject(), Serializable