package s.yarlykov.minipaint.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridItemDecoration(var rows: Int, var cols : Int) : RecyclerView.ItemDecoration() {

    /**
     * @outRect в своих полях left, top, bottom, right возвращает значения padding'ов
     * для данного view. То есть это не прямоугольник с координатами или динами сторон.
     * Это просто массив из 4-х чисел значений отступов.
     *
     * Кроме отступов ещё явно указываем размер view. В данном случае все view должны
     * располагаться без отступов прилегая друг к другу.
     */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        with(outRect) {
            bottom = 0
            top = 0
            left = 0
            right = 0
        }

        val parentWidth = parent.measuredWidth
        val parentHeight = parent.measuredHeight

        view.apply {
            (layoutParams as GridLayoutManager.LayoutParams).apply {
                width = parentWidth / cols
                height= parentHeight / rows
            }.let {
                layoutParams = it
            }
        }
    }
}