package s.yarlykov.minipaint.view.custom

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View.MeasureSpec.EXACTLY
import android.widget.GridLayout
import androidx.annotation.Dimension
import androidx.annotation.Dimension.DP
import s.yarlykov.minipaint.model.Color
import s.yarlykov.minipaint.model.getColorInt
import s.yarlykov.minipaint.model.getColorRes

private const val COLUMNS_PREF = 3

class ColorPickerView : GridLayout {

    companion object {
        private const val PALETTE_ANIMATION_DURATION = 150L
        private const val HEIGHT = "height"
        private const val SCALE = "scale"
        @Dimension(unit = DP)
        private const val COLOR_VIEW_PADDING = 8
    }

    var onColorClickListener: (color: Color) -> Unit = { }

    val isOpen: Boolean
        get() = measuredHeight > 0

    private var desiredHeight = 0

    private val animator by lazy {
        ValueAnimator().apply {
            duration = PALETTE_ANIMATION_DURATION
            addUpdateListener(updateListener)
        }
    }

    private val updateListener by lazy {
        ValueAnimator.AnimatorUpdateListener { animator ->
            layoutParams.apply {
                height = animator.getAnimatedValue(HEIGHT) as Int
            }.let {
                layoutParams = it
            }

            val scaleFactor = animator.getAnimatedValue(SCALE) as Float
            for (i in 0 until childCount) {
                getChildAt(i).apply {
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                    alpha = scaleFactor
                }
            }
        }
    }

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        orientation = HORIZONTAL
        columnCount = COLUMNS_PREF

        Color.values().forEach { color ->
            val colorView =
                ColorView(context).apply {
                    fillColorRes = color.getColorRes()
                    fillColorInt = color.getColorInt(context)
                    tag = color
//                    dip(COLOR_VIEW_PADDING).let {
//                        setPadding(it, it, it, it)
//                    }
//                    setOnClickListener { onColorClickListener(it.tag as Color) }
                }
            addView(colorView)
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val pickerW = MeasureSpec.getSize(widthSpec)
        val pickerH = MeasureSpec.getSize(heightSpec)

        // Для положения Landscape увеличиваем кол-во столбцов
        if (pickerW > pickerH) {
            columnCount = childCount / COLUMNS_PREF
        }

        val childSpecWidth = MeasureSpec.makeMeasureSpec(pickerW / columnCount, EXACTLY)
        val childSpecHeight =
            MeasureSpec.makeMeasureSpec(pickerH / (childCount / columnCount), EXACTLY)

        (0 until childCount).forEach { i ->
            val v = getChildAt(i)

            if (v is ColorView) {
//                measureChild(v, childSpecWidth, childSpecHeight)
                v.measure(childSpecWidth, childSpecHeight)
            }
        }
        setMeasuredDimension(MeasureSpec.getSize(widthSpec), MeasureSpec.getSize(heightSpec))
    }

    fun open() {
        animator.cancel()
        animator.setValues(
            PropertyValuesHolder.ofInt(HEIGHT, measuredHeight, desiredHeight),
            PropertyValuesHolder.ofFloat(SCALE, getChildAt(0).scaleX, 1f)
        )
        animator.start()
    }

    fun close() {
        animator.cancel()
        animator.setValues(
            PropertyValuesHolder.ofInt(HEIGHT, measuredHeight, 0),
            PropertyValuesHolder.ofFloat(SCALE, getChildAt(0).scaleX, 0f)
        )
        animator.start()
    }
}