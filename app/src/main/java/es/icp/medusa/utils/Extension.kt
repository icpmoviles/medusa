package es.icp.medusa.utils

import android.animation.ObjectAnimator
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.res.ResourcesCompat
import com.google.gson.Gson
import es.icp.medusa.R
import org.json.JSONObject

fun Any.toJson(): JSONObject =
    JSONObject(Gson().toJson(this).toString())

fun View.createBlur() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.setRenderEffect(
            RenderEffect.createBlurEffect(5F, 5F, Shader.TileMode.CLAMP)
        )
    } else this.setBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.disabled, null))
}

fun View.removeBlur(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.setRenderEffect(null)
    } else this.background = null
}


const val ANIMATION_DURATION = 1250L

fun View.rotateYForever(
    duration: Long = ANIMATION_DURATION,
    repeatCount: Int = ObjectAnimator.INFINITE,
) {
    val animation = ObjectAnimator.ofFloat(this, "rotationY", 0f, 360f)
    animation.duration = duration
    animation.repeatCount = repeatCount
    animation.interpolator = LinearInterpolator()
    animation.start()
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}