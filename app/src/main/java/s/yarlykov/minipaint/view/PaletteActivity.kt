package s.yarlykov.minipaint.view

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.content_palette.*
import s.yarlykov.minipaint.R

class PaletteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_palette)

        // Заливка градиентом сверху вниз
        paletteLayout.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                ResourcesCompat.getColor(resources, R.color.colorPrimary, null),
                ResourcesCompat.getColor(resources, android.R.color.white, null)
            )
        )

        initViews()
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
            colorPicker.setPreviewColors(bg to fg)
        }

        // Cancel - закрыть активити
        buttonCancel.setOnClickListener {
            (paletteLayout as MotionLayout).transitionToStart()
        }

        // OK - проверить, что цвета валидные и вернуть результат в MainActivity.
        // Одинаковые цвета фона и кисти не разрешаются.
        buttonOk.setOnClickListener {
            with(colorPicker) {

                if ((chosenBackground and 0xFF000000.toInt()) != 0 &&
                    (chosenForeground and 0xFF000000.toInt()) != 0 &&
                    (chosenBackground != chosenForeground)
                ) {
                    sendResult()
                    finish()
                }
            }
        }

        (paletteLayout as MotionLayout).setTransitionListener(
            object : MotionLayout.TransitionListener {
                override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
                }
                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                }
                override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
                }
                override fun onTransitionCompleted(p0: MotionLayout?, constraintSetId: Int) {
                    if(constraintSetId == R.id.start) {
                        this@PaletteActivity.finish()
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        (paletteLayout as MotionLayout).transitionToEnd()
    }

    // Вернуть результат в MainActivity
    private fun sendResult() {
        setResult(Activity.RESULT_OK,
            Intent().apply {
                putExtra(getString(R.string.key_bg), colorPicker.chosenBackground)
                putExtra(getString(R.string.key_fg), colorPicker.chosenForeground)
            }
        )
    }
}
