package s.yarlykov.minipaint.view

import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.graphics.Rect
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridItemDecoration(var rows: Int, var cols : Int) : RecyclerView.ItemDecoration() {

    /**
     * @outRect в своих полях left, top, bottom, right возвращает значения padding'ов
     * для данного view. То есть это не прямоугольник с координатами или динами сторон.
     * Это просто массив из 4-х чисел значений отступов.
     */

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        val wm = parent.context.getSystemService(WINDOW_SERVICE) as WindowManager

        val parentWidth = with(Point()) {
            wm.defaultDisplay.getSize(this)
            this.x
        }

        val parentHeight = with(Point()) {
            wm.defaultDisplay.getSize(this)
            this.y
        }

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