package s.yarlykov.minipaint.model

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import s.yarlykov.minipaint.R

enum class Color {
    RED,
    PINK,
    PURPLE,
    DEEP_PURPLE,
    INDIGO,
    BLUE,
    LIGHT_BLUE,
    CYAN,
    TEAL,
    GREEN,
    LIGHT_GREEN,
    LIME,
    YELLOW,
    AMBER,
    ORANGE,
    DEEP_ORANGE,
    BROWN,
    GREY,
    BLUE_GREY,
    WHITE,
    BLACK
}

fun Color.getColorInt(context: Context) =
    ContextCompat.getColor(
        context, when (this) {
            Color.RED -> R.color.red
            Color.PINK -> R.color.pink
            Color.PURPLE -> R.color.purple

            Color.DEEP_PURPLE -> R.color.deep_purple
            Color.INDIGO -> R.color.indigo
            Color.BLUE -> R.color.blue

            Color.LIGHT_BLUE -> R.color.light_blue
            Color.CYAN -> R.color.cyan
            Color.TEAL -> R.color.teal

            Color.GREEN -> R.color.green
            Color.LIGHT_GREEN -> R.color.light_green
            Color.LIME -> R.color.lime

            Color.YELLOW -> R.color.yellow
            Color.AMBER -> R.color.amber
            Color.ORANGE -> R.color.orange

            Color.DEEP_ORANGE -> R.color.deep_orange
            Color.BROWN -> R.color.brown
            Color.GREY -> R.color.grey

            Color.BLUE_GREY -> R.color.blue_grey
            Color.WHITE -> R.color.white
            Color.BLACK -> R.color.black
        }
    )

fun Color.getColorRes(): Int = when (this) {
    Color.RED -> R.color.red
    Color.PINK -> R.color.pink
    Color.PURPLE -> R.color.purple

    Color.DEEP_PURPLE -> R.color.deep_purple
    Color.INDIGO -> R.color.indigo
    Color.BLUE -> R.color.blue

    Color.LIGHT_BLUE -> R.color.light_blue
    Color.CYAN -> R.color.cyan
    Color.TEAL -> R.color.teal

    Color.GREEN -> R.color.green
    Color.LIGHT_GREEN -> R.color.light_green
    Color.LIME -> R.color.lime

    Color.YELLOW -> R.color.yellow
    Color.AMBER -> R.color.amber
    Color.ORANGE -> R.color.orange

    Color.DEEP_ORANGE -> R.color.deep_orange
    Color.BROWN -> R.color.brown
    Color.GREY -> R.color.grey

    Color.BLUE_GREY -> R.color.blue_grey
    Color.WHITE -> R.color.white
    Color.BLACK -> R.color.black
}