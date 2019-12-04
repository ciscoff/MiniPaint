package s.yarlykov.minipaint.view.custom

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
import android.widget.GridLayout
import androidx.annotation.Dimension
import androidx.annotation.Dimension.DP
import s.yarlykov.minipaint.R
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

        val count = Color.values().size

        Color.values().withIndex().forEach { item ->

            val view =

                // Элемент палитры (ColorView)
                if (item.index != count / 2) {

                    ColorView(context).apply {
                        fillColorRes = item.value.getColorRes()
                        fillColorInt = item.value.getColorInt(context)
                        tag = item.value
//                    setOnClickListener { onColorClickListener(it.tag as Color) }
                    }
                } else {
                    // Центральный элемент - превью выбранных цветов (MaterialCard)
                    LayoutInflater
                        .from(context)
                        .inflate(R.layout.layout_preview, this, false)
                        .apply {
                            (layoutParams as GridLayout.LayoutParams).apply {
                                setGravity(Gravity.CENTER)
                            }.let {
                                layoutParams = it
                            }
                        }
                }

            addView(view)
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val pickerW = MeasureSpec.getSize(widthSpec)
        val pickerH = MeasureSpec.getSize(heightSpec)

        // Для положения Landscape увеличиваем кол-во столбцов
        if (pickerW > pickerH) {
            columnCount = childCount / COLUMNS_PREF
        }

        // Предпочтительные размеры для ребенка
        val childPrefW = pickerW / columnCount
        val childPrefH = pickerH / (childCount / columnCount)

        (0 until childCount).forEach { i ->
            val v = getChildAt(i)

            if (v is ColorView) {
                childMeasure(v, childPrefW, childPrefH, EXACTLY)
            } else {
                childMeasure(v, childPrefW - childPrefW / 6, childPrefH - childPrefH / 6, AT_MOST)
            }
        }
        setMeasuredDimension(MeasureSpec.getSize(widthSpec), MeasureSpec.getSize(heightSpec))
    }

    // Вызывать measure у дочернего элемента
    private fun childMeasure(child: View, w: Int, h: Int, mode: Int) {
        val childSpecWidth = MeasureSpec.makeMeasureSpec(w, mode)
        val childSpecHeight = MeasureSpec.makeMeasureSpec(h, mode)
        child.measure(childSpecWidth, childSpecHeight)
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