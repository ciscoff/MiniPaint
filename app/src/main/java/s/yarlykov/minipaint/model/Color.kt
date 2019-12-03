package s.yarlykov.minipaint.model

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import s.yarlykov.minipaint.R

enum class Color {
    WHITE,
    YELLOW,
    GREEN,
    BLUE,
    RED,
    VIOLET,
    PINK,
    BG
}

fun Color.getColorInt(context: Context) =
    ContextCompat.getColor(
        context, when (this) {
            Color.WHITE -> R.color.white
            Color.YELLOW -> R.color.yellow
            Color.GREEN -> R.color.green
            Color.BLUE -> R.color.blue
            Color.RED -> R.color.red
            Color.VIOLET -> R.color.violet
            Color.PINK -> R.color.pink
            Color.BG -> R.color.colorBackground
        }
    )

fun Color.getColorRes(): Int = when (this) {
    Color.WHITE -> R.color.white
    Color.VIOLET -> R.color.violet
    Color.YELLOW -> R.color.yellow
    Color.RED -> R.color.red
    Color.PINK -> R.color.pink
    Color.GREEN -> R.color.green
    Color.BLUE -> R.color.blue
    Color.BG -> R.color.colorBackground
}