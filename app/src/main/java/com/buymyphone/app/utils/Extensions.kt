package com.buymyphone.app.utils

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.visible() { visibility = View.VISIBLE }
fun View.gone()    { visibility = View.GONE    }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.animateFadeIn(duration: Long = 300) {
    alpha = 0f
    visible()
    animate().alpha(1f).setDuration(duration).start()
}

fun View.animateScale(from: Float = 0.85f, to: Float = 1f, duration: Long = 300) {
    scaleX = from
    scaleY = from
    animate().scaleX(to).scaleY(to).setDuration(duration).start()
}

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toShortDate(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.bytesToMB(): Long = this / (1024 * 1024)
fun Long.bytesToGB(): Double = this / (1024.0 * 1024.0 * 1024.0)

fun Double.roundTo(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

fun Context.dpToPx(dp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
    ).toInt()
}

fun Int.scoreToColor(): Int {
    return when {
        this >= 85 -> android.graphics.Color.parseColor("#4CAF50")
        this >= 70 -> android.graphics.Color.parseColor("#2196F3")
        this >= 50 -> android.graphics.Color.parseColor("#FF9800")
        else       -> android.graphics.Color.parseColor("#F44336")
    }
}

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }
