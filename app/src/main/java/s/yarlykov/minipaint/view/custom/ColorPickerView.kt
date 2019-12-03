package s.yarlykov.minipaint.view.custom

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.annotation.Dimension
import androidx.annotation.Dimension.DP
import androidx.constraintlayout.widget.ConstraintLayout
import org.jetbrains.anko.dip
import s.yarlykov.minipaint.model.Color
import s.yarlykov.minipaint.model.getColorInt
import s.yarlykov.minipaint.model.getColorRes

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
        columnCount = 2

        Color.values().forEach { color ->

            val colorView =
                ColorView(context).apply {
                    fillColorRes = color.getColorRes()
                    fillColorInt = color.getColorInt(context)
                    tag = color
                    preferredDims = measuredWidth/columnCount to measuredWidth/4
//                    dip(COLOR_VIEW_PADDING).let {
//                        setPadding(it, it, it, it)
//                    }
//                    setOnClickListener { onColorClickListener(it.tag as Color) }
                }

            addView(colorView)
            (colorView.layoutParams as LayoutParams).columnSpec = spec(UNDEFINED, 1f)
            Log.d("DIMS", "w.h = $width.$height")

        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        layoutParams.apply {
            desiredHeight = height
            height = 0
        }.let {
            layoutParams = it
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {

        (0 until childCount).forEach {i ->
            val v = getChildAt(i)

            if(v is ColorView) {
                v.preferredDims = width/columnCount to height/6
            }
        }
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