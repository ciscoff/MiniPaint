package s.yarlykov.minipaint.view.custom

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
import android.widget.GridLayout
import android.widget.ImageView
import com.google.android.material.card.MaterialCardView
import org.jetbrains.anko.configuration
import s.yarlykov.minipaint.R
import s.yarlykov.minipaint.model.Color
import s.yarlykov.minipaint.model.getColorInt
import s.yarlykov.minipaint.model.getColorRes

private const val COLUMNS_PREF = 3

class ColorPickerView : GridLayout {

    /**
     * Цвета фона и кисти, выбранные пользователем
     */
    var chosenBackground: Int = 0
        private set
    var chosenForeground: Int = 0
        private set

    /**
     * @choicePreview - элемент для отрисовки пользовательского выбора
     * @isBackgroundSelected - флаг. Цвета фона и кисти выбираются по очереди.
     * Флаг показывает что было выбрано очередным кликом по ColorView.
     */
    private lateinit var choicePreview: View
    private var isBackgroundSelected = true

    /**
     * Вызывается при клике на каждом элементе палитры. По очереди меняет
     * цвета фон/кисть.
     */
    var onColorClickListener: (color : Int) -> Unit = { color ->
        if (isBackgroundSelected) {
            setPreviewColors(color to 0)
        } else {
            setPreviewColors(0 to color)
        }
        isBackgroundSelected = !isBackgroundSelected
    }

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        // По какой оси "нумеровать" столбцы
        orientation = HORIZONTAL

        // Массив цветов палитры
        val colors = context.resources.obtainTypedArray(R.array.palette_resources)
        val count = colors.length()

        // Для положения Landscape увеличиваем кол-во столбцов
        columnCount =
        if(context.configuration.orientation == ORIENTATION_PORTRAIT) {
            COLUMNS_PREF
        } else {
            count / COLUMNS_PREF
        }

        /**
         * Проход по всем цветам палитры. Вместо среднего элемента поместим в таблицу
         * choicePreview для отображения пользовательского выбора.
         */

        (0 until count).forEach { index ->
            val view =

                // Элемент палитры (ColorView)
                if (index != count / 2 ) {

                    ColorView(context).apply {
                        fillColorInt = colors.getColor(index, 0)
                        tag = fillColorInt
                        setOnClickListener {view ->
                            animate(view)
                            onColorClickListener(fillColorInt)
                        }
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
                            choicePreview = this
                        }
                }
            addView(view)
        }

        colors.recycle()
    }

    /**
     * В портретной ориентации таблица размерности AxB
     * В алаьбомной - BxA
     */
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val pickerW = MeasureSpec.getSize(widthSpec)
        val pickerH = MeasureSpec.getSize(heightSpec)

        // Предпочтительные размеры для ребенка
        val childPrefW = pickerW / columnCount
        val childPrefH = pickerH / (childCount / columnCount)

        (0 until childCount).forEach { i ->
            val v = getChildAt(i)

            if (v is ColorView) {
                childMeasure(v,
                    childPrefW,
                    childPrefH,
                    MeasureSpec.getMode(widthSpec),
                    MeasureSpec.getMode(heightSpec))
            } else {
                childMeasure(v,
                    childPrefW - childPrefW/4,
                    childPrefH - childPrefH/4,
                    AT_MOST, AT_MOST)
            }
        }
        setMeasuredDimension(MeasureSpec.getSize(widthSpec), MeasureSpec.getSize(heightSpec))

        // После определения размеров требуется принудительно выполнить layout, иначе
        // все дочерние элементы (ColorView) позиционируются неверно.
        requestLayout()
    }

    // Анимация масштабированием (уменьшение размера и восстановление)
    private fun animate(view : View) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            view, scaleX, scaleY)
        animator.repeatCount = 1
        animator.duration = 100
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.start()
    }

    // Вызвать measure у дочернего элемента
    private fun childMeasure(child: View, w: Int, h: Int, modeW: Int, modeH : Int) {
        val childSpecWidth = MeasureSpec.makeMeasureSpec(w, modeW)
        val childSpecHeight = MeasureSpec.makeMeasureSpec(h, modeH)
        child.measure(childSpecWidth, childSpecHeight)
    }

    // Вызывается при необходимости обновить цвета в preview в том числе и из
    // родительской PaletteActivity
    fun setPreviewColors(colors: Pair<Int, Int>) {

        if (colors.first != 0) {
            chosenBackground = colors.first
            (choicePreview as MaterialCardView).setCardBackgroundColor(chosenBackground)
        }

        if (colors.second != 0) {
            chosenForeground = colors.second
            choicePreview.findViewById<ImageView>(R.id.ivPreview)
                .setColorFilter(chosenForeground, android.graphics.PorterDuff.Mode.SRC_IN)
        }
    }
}