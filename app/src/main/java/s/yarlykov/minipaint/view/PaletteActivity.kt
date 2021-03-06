package s.yarlykov.minipaint.view

import android.animation.*
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.transition.*
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_palette.*
import kotlinx.android.synthetic.main.content_palette.buttonCancel
import kotlinx.android.synthetic.main.content_palette.buttonOk
import org.jetbrains.anko.configuration
import s.yarlykov.minipaint.R
import s.yarlykov.minipaint.view.custom.ColorAdapter
import s.yarlykov.minipaint.view.custom.ColorView
import s.yarlykov.minipaint.view.custom.GridItemDecoration

private const val COLUMNS_PREF = 3

class PaletteActivity : AppCompatActivity(), ChoiceHandler {

    /**
     * Цвета фона и кисти, выбранные пользователем
     */
    private var chosenBackground: Int = 0
    private var chosenForeground: Int = 0

    /**
     * Две ImageView. На одной отображается цвет фона на другой цвет кисти
     */
    private lateinit var viewBackground: ImageView
    private lateinit var viewForeground: ImageView

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

        initControlViews()

        initRecyclerView()
    }

    override fun onPause() {
        super.onPause()

        // Из-за оператора ниже Activity не закрывалась после finish()

        // Переопределить анимацию закрытия окна актитиви
        // https://developer.android.com/reference/android/app/Activity.html#overridePendingTransition(int,%20int)
//        overridePendingTransition(0, R.transition.fade_out_transition)
    }

    override fun onPreviewCreated(bgView: View, fgView: View) {
        viewBackground = bgView as ImageView
        viewForeground = fgView as ImageView

        if (chosenBackground != 0 && chosenForeground != 0) {
            setPreviewColors(chosenBackground to chosenForeground)
        }
    }

    override fun onColorClicked(view: ColorView) {

        animateColorChoice(view)

        if (isBackgroundSelected) {
            setPreviewColors(view.fillColorInt to 0)
        } else {
            setPreviewColors(0 to view.fillColorInt)
        }
        isBackgroundSelected = !isBackgroundSelected
    }

    /**
     * В интенте из MainActivity прилетают текущие цвета экрана рисования.
     * Их нужно передать в ColorPicker для отрисовки элемента preview.
     */
    private fun initControlViews() {

        with(intent) {
            chosenBackground = getIntExtra(
                getString(R.string.key_bg),
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorBackground, null
                )
            )

            chosenForeground = getIntExtra(
                getString(R.string.key_fg),
                ResourcesCompat.getColor(resources, R.color.colorPaint, null)
            )
        }

        // Cancel - закрыть активити
        buttonCancel.setOnClickListener {
            animateButtonsAndExit()
        }

        // OK - проверить, что цвета валидные и вернуть результат в MainActivity.
        // Одинаковые цвета фона и кисти не разрешаются.
        buttonOk.setOnClickListener {
            if ((chosenBackground and 0xFF000000.toInt()) != 0 &&
                (chosenForeground and 0xFF000000.toInt()) != 0 &&
                (chosenBackground != chosenForeground)
            ) {
                animateColorsAndExit(viewBackground)
            }
        }
    }

    private fun initRecyclerView() {

        // Массив идентификаторов ресурсов цветов палитры и количество цветов
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
            setGrid(rows, columns)
            addItemDecoration(GridItemDecoration(rows, columns))
            layoutManager = GridLayoutManager(this@PaletteActivity, columns)
            adapter = ColorAdapter(rows, columns, colorArray, this@PaletteActivity)
        }
    }

    // Вызывается при необходимости обновить цвета в preview.
    private fun setPreviewColors(colors: Pair<Int, Int>) {

        if (colors.first != 0) {
            chosenBackground = colors.first
            viewBackground
                .setColorFilter(chosenBackground, android.graphics.PorterDuff.Mode.SRC_IN)
        }

        if (colors.second != 0) {
            chosenForeground = colors.second
            viewForeground
                .setColorFilter(chosenForeground, android.graphics.PorterDuff.Mode.SRC_IN)
        }
    }

    // Анимация масштабированием (уменьшение размера и восстановление)
    private fun animateColorChoice(view: View) {
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

    // Анимация кнопок (уход вправо, влево).
    // Активити закрывается в animatorListener.
    private fun animateButtonsAndExit() {

        val set = AnimatorSet()
        set.playTogether(
            ObjectAnimator.ofFloat(buttonCancel, View.TRANSLATION_X, -500f).apply {
                repeatCount = 0
            },
            ObjectAnimator.ofFloat(buttonOk, View.TRANSLATION_X, 500f).apply {
                repeatCount = 0
            }
        )
        set.addListener(animatorListener)
        set.duration = 300
        set.start()
    }

    // Анимация шариков (explosion из центра)
    // Активити закрывается из transitionListener
    private fun animateColorsAndExit(view: View) {

        val viewRect = Rect()
        view.getGlobalVisibleRect(viewRect)

        val explodeTransition = Explode()

        explodeTransition.epicenterCallback = object : Transition.EpicenterCallback() {
            override fun onGetEpicenter(transition: Transition): Rect {
                return viewRect
            }
        }

        explodeTransition.excludeTarget(view, true)

        val set = TransitionSet().apply {
            addTransition(explodeTransition)
            addTransition(Fade().addTarget(view))
            addListener(transitionListener)
        }

        TransitionManager.beginDelayedTransition(recyclerView, set)

        // Триггер начала анимации. Удаляем контент и все начинается
        recyclerView.adapter = null
    }

    // Вернуть результат в MainActivity
    private fun sendResultAndFinish() {

        this@PaletteActivity.setResult(Activity.RESULT_OK,
            Intent().apply {
                putExtra(getString(R.string.key_bg), chosenBackground)
                putExtra(getString(R.string.key_fg), chosenForeground)
            }
        )
        this@PaletteActivity.finish()
    }

    // Анимация шариков
    private val transitionListener = object : Transition.TransitionListener {
        override fun onTransitionEnd(transition: Transition) {
            sendResultAndFinish()
        }

        override fun onTransitionResume(transition: Transition) {
        }

        override fun onTransitionPause(transition: Transition) {
        }

        override fun onTransitionCancel(transition: Transition) {
        }

        override fun onTransitionStart(transition: Transition) {
        }
    }

    // Анимация кнопок
    private val animatorListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            this@PaletteActivity.finish()
        }
    }
}
