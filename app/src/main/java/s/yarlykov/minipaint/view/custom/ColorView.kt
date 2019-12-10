package s.yarlykov.minipaint.view.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import s.yarlykov.minipaint.R
import kotlin.math.min

/**
 * Отдельный цветовой элемент в палитре.
 * Обработчки кликов инжектится в ColorPickerView.
 */
class ColorView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var radius : Float = 0f

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
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

    /**
     * Просто принимаем предлженные размеры
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        canvas.drawColor(fillColorInt)
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, fillPaint)
    }
}