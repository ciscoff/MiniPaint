package s.yarlykov.minipaint.view.custom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.minipaint.R
import s.yarlykov.minipaint.view.ChoiceHandler

class ColorAdapter(
    private val rows: Int,
    private val columns: Int,
    private val colors: List<Int>,
    private val choiceHandler: ChoiceHandler
) :
    RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    // Назначить типы элементам
    override fun getItemViewType(position: Int): Int {
//            return if(position == columns / 2) {
        return if (position == rows * columns / 2) {
            ItemType.PREVIEW.ordinal
        } else {
            ItemType.COLOR.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return when (ItemType.values()[viewType]) {
            ItemType.COLOR -> {
                ViewHolder(ColorView(parent.context).apply {
                    setOnClickListener { view ->
                        choiceHandler.onColorClicked(view as ColorView)
                    }
                    tag = ItemType.COLOR
                })
            }
            ItemType.PREVIEW -> {
                val view = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.layout_preview, parent, false).apply {
                        tag = ItemType.PREVIEW
                    }
                choiceHandler.onPreviewCreated(
                    view.findViewById(R.id.ivBackground),
                    view.findViewById(R.id.ivForeground)
                )
                ViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return columns * rows
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder.itemView) {
            if (tag == ItemType.COLOR) {
                (this as ColorView).fillColorInt = colors[position]
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    enum class ItemType {
        COLOR,
        PREVIEW
    }
}