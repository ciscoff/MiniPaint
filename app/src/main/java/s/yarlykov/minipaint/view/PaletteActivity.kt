package s.yarlykov.minipaint

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.content_palette.*

class PaletteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_palette)
        initViews()
    }

    private fun initViews() {

        with(intent){
            val bg = getIntExtra(
                getString(R.string.key_bg),
                ResourcesCompat.getColor(resources, R.color.colorBackground, null)
            )

            val fg = getIntExtra(
                getString(R.string.key_fg),
                ResourcesCompat.getColor(resources, R.color.colorPaint, null)
            )
            colorPicker.setPreviewColors(bg to fg)
        }

        buttonCancel.setOnClickListener {
            finish()
        }

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
    }

    private fun sendResult() {
        setResult(Activity.RESULT_OK,
            Intent().apply {
                putExtra(getString(R.string.key_bg), colorPicker.chosenBackground)
                putExtra(getString(R.string.key_fg), colorPicker.chosenForeground)
            }
        )
    }
}
