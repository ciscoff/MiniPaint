package s.yarlykov.minipaint.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.solver.widgets.analyzer.BasicMeasure.AT_MOST
import androidx.recyclerview.widget.RecyclerView
import java.lang.Integer.MAX_VALUE

class ColorPickerRv : RecyclerView {

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var columnCount: Int = 1
    private var rowCount: Int = 1

    fun setGrid(rows: Int, cols: Int) {
        columnCount = cols
        rowCount = rows
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
        val childPrefH = pickerH / rowCount

        (0 until childCount).forEach { i ->
            val v = getChildAt(i)

            childMeasure(
                v,
                childPrefW,
                childPrefH,
                MeasureSpec.EXACTLY,
                MeasureSpec.EXACTLY
            )
        }
        setMeasuredDimension(MeasureSpec.getSize(widthSpec), MeasureSpec.getSize(heightSpec))

        // После определения размеров требуется принудительно выполнить layout, иначе
        // все дочерние элементы (ColorView) позиционируются неверно.
//        requestLayout()
    }

    // Вызвать measure у дочернего элемента
    private fun childMeasure(child: View, w: Int, h: Int, modeW: Int, modeH: Int) {
        val childSpecWidth = MeasureSpec.makeMeasureSpec(w, modeW)
        val childSpecHeight = MeasureSpec.makeMeasureSpec(h, modeH)
        child.measure(childSpecWidth, childSpecHeight)
    }

}