package s.yarlykov.minipaint.view.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import s.yarlykov.minipaint.R

class ColorView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    var preferredDims: Pair<Int, Int> = 128 to 128
        set(value) {
            if(value.first > 0 && value.second > 0) {
                field = value

                layoutParams.apply {
                    width = value.first
                    height = value.second
                }.let {
                    layoutParams = it
                }
            }
        }

    /**
     * @ColorRes - это ссылка на ресурс, то есть R.color.black
     * @ColorInt - это непосредственное значение цвета, 32-х битное (включая alpha)
     */

    @ColorRes
    var fillColorRes: Int = R.color.white
        set(value) {
            field = value
            fillPaint.color = ContextCompat.getColor(context, value)
        }

    @ColorInt
    var fillColorInt: Int = ResourcesCompat.getColor(resources, R.color.white, null)
        set(value) {
            field = value
            fillPaint.color = value
        }

    init {
        isClickable = true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("DIMS","ColorView Dims = $w.$h")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(fillColorInt)
    }
}