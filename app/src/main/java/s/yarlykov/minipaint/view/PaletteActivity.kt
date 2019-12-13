package s.yarlykov.minipaint.view

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.activity_palette.*
import kotlinx.android.synthetic.main.content_palette.*
import kotlinx.android.synthetic.main.content_palette.buttonCancel
import kotlinx.android.synthetic.main.content_palette.buttonOk
import org.jetbrains.anko.configuration
import s.yarlykov.minipaint.R
import s.yarlykov.minipaint.view.custom.ColorView

private const val COLUMNS_PREF = 3

class PaletteActivity : AppCompatActivity() {

    /**
     * Цвета фона и кисти, выбранные пользователем
     */
    private var chosenBackground: Int = 0
        private set
    private var chosenForeground: Int = 0
        private set

    /**
     * @isBackgroundSelected - флаг. Цвета фона и кисти выбираются по очереди.
     * Флаг показывает что было выбрано очередным кликом по ColorView.
     */
    private var isBackgroundSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_palette)

        // Заливка градиентом сверху вниз
        paletteActivityLayout.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                ResourcesCompat.getColor(resources, R.color.colorPrimary, null),
                ResourcesCompat.getColor(resources, android.R.color.white, null)
            )
        )

        initViews()

        initRecyclerView()
    }

    /**
     * В интенте из MainActivity прилетают текущие цвета экрана рисования.
     * Их нужно передать в ColorPicker для отрисовки элемента preview.
     */
    private fun initViews() {

        with(intent) {
            val bg = getIntExtra(
                getString(R.string.key_bg),
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorBackground, null
                )
            )

            val fg = getIntExtra(
                getString(R.string.key_fg),
                ResourcesCompat.getColor(resources, R.color.colorPaint, null)
            )
            setPreviewColors(bg to fg)
        }

        // Cancel - закрыть активити
        buttonCancel.setOnClickListener {
            onBackPressed()
        }

        // OK - проверить, что цвета валидные и вернуть результат в MainActivity.
        // Одинаковые цвета фона и кисти не разрешаются.
        buttonOk.setOnClickListener {
            if ((chosenBackground and 0xFF000000.toInt()) != 0 &&
                (chosenForeground and 0xFF000000.toInt()) != 0 &&
                (chosenBackground != chosenForeground)
            ) {
                sendResult()
                finish()
            }
        }
    }

    // Вернуть результат в MainActivity
    private fun sendResult() {
        setResult(Activity.RESULT_OK,
            Intent().apply {
                putExtra(getString(R.string.key_bg), chosenBackground)
                putExtra(getString(R.string.key_fg), chosenForeground)
            }
        )
    }

    private fun initRecyclerView() {

        // Массив идентификаторов ресурсов цветов палитры
        val colors = resources.obtainTypedArray(R.array.palette_resources)
        val colorsQty = colors.length()

        // Массив значений цветов
        val colorArray = mutableListOf<Int>().apply {
            (0 until colorsQty).forEach { index ->
                this.add(colors.getColor(index, 0))
            }
        }
        colors.recycle()

        // Для положения Landscape увеличиваем кол-во столбцов
        val columns =
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                COLUMNS_PREF
            } else {
                colorsQty / COLUMNS_PREF
            }
        val rows = colorsQty / columns

        with(recyclerView) {
//            setNestedScrollingEnabled(false)
            setDimensions(rows, columns)
            addItemDecoration(GridItemDecoration(rows, columns))
            layoutManager = GridLayoutManager(this@PaletteActivity, columns)
            adapter = Adapter(rows, columns, colorArray)
        }
    }

    // Вызывается при необходимости обновить цвета в preview.
    private fun setPreviewColors(colors: Pair<Int, Int>) {

        if (colors.first != 0) {
            chosenBackground = colors.first
            (cardChoicePreview as MaterialCardView).setCardBackgroundColor(chosenBackground)
        }

        if (colors.second != 0) {
            chosenForeground = colors.second
            ivChoicePreview.findViewById<ImageView>(R.id.ivChoicePreview)
                .setColorFilter(chosenForeground, android.graphics.PorterDuff.Mode.SRC_IN)
        }
    }

    // Анимация масштабированием (уменьшение размера и восстановление)
    private fun animate(view: View) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            view, scaleX, scaleY
        )
        animator.repeatCount = 1
        animator.duration = 100
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.start()
    }

    /**
     *
     */
    inner class Adapter(private val rows: Int, private val columns: Int, val colors: List<Int>) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ColorView(this@PaletteActivity).apply {
                setOnClickListener {
                    this@PaletteActivity.animate(this)
                }
            })
        }

        override fun getItemCount(): Int {
            return columns * rows
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            with(holder.itemView) {
                (this as ColorView).fillColorInt = colors[position]
                this.tag = colors[position]
            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}
