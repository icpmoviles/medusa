package es.icp.medusa.utils

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import es.icp.icp_commons.DxCustom.DxCustom
import es.icp.medusa.R

object Dx {

    fun dxSinConexion (context: Context, onClickButtonAction: () -> Unit){
        DxCustom(context).createDialog(true)
            .setTitulo("Sin conexión")
            .setMensaje("No dispones de conexión a internet.")
            .noPermitirSalirSinBotones()
            .setIcono(
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_perseo_logo_casco, null)
            )
            .showAceptarButton(
                "Aceptar",
                ResourcesCompat.getColor(context.resources, R.color.primary, null)
            ){ onClickButtonAction.invoke() }
            .showDialogReturnDxCustom()
    }

    fun dxWebServiceError (context: Context,titulo: String, mensaje: String, onClickButtonAction: () -> Unit){
        DxCustom(context).createDialog(true)
            .setTitulo(titulo)
            .setMensaje(mensaje)
            .noPermitirSalirSinBotones()
            .setIcono(
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_perseo_logo_casco, null), null
            )
            .showAceptarButton("Aceptar"){ onClickButtonAction.invoke() }
            .showDialogReturnDxCustom()


    }
}