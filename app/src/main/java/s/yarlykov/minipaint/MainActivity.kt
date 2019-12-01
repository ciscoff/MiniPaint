package s.yarlykov.minipaint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import s.yarlykov.minipaint.presentation.PaintView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val paintView = PaintView(this)
        paintView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        paintView.contentDescription = getString(R.string.paint_view_description)
        setContentView(paintView)
    }
}
