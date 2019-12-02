package s.yarlykov.minipaint

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.minipaint.domain.Interactor
import s.yarlykov.minipaint.domain.InteractorImpl
import s.yarlykov.minipaint.presentation.PaintView

class MainActivity : AppCompatActivity() {

    lateinit var interactor: Interactor
    lateinit var paintView : PaintView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        interactor = InteractorImpl(this)

        paintView = PaintView(this)
        paintView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        paintView.contentDescription = getString(R.string.paint_view_description)
        setContentView(paintView)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tools_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuShare -> {
                interactor.share(paintView.getBitmap())
                true
            }
            R.id.menuUndoAll -> {
                paintView.resetAll()
                true
            }
            R.id.menuUndo -> {
                paintView.resetLast()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
