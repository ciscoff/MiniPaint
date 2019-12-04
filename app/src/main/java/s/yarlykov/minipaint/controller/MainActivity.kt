package s.yarlykov.minipaint.controller

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.startActivity
import s.yarlykov.minipaint.BuildConfig
import s.yarlykov.minipaint.PaletteActivity
import s.yarlykov.minipaint.view.PaintView
import s.yarlykov.minipaint.PathStack
import s.yarlykov.minipaint.R
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    lateinit var paintView: PaintView

    private val disposable = CompositeDisposable()
    private val pathStack = PathStack()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paintView = PaintView(this, pathStack)
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
                share(paintView.getBitmap())
                true
            }
            R.id.menuUndoAll -> {
                pathStack.clear()
                paintView.onDataChanged()
                true
            }
            R.id.menuUndo -> {
                pathStack.pop()
                paintView.onDataChanged()
                true
            }
            R.id.menuPalette -> {
                startPaletteActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun startPaletteActivity() {
//        startActivity(Intent(this, PaletteActivity::class.java).apply {
//            putExtra(getString(R.string.key_bg), paintView.)
//            putExtra(getString(R.string.key_fg), note)
//        })
        startActivity<PaletteActivity>()
    }

    private fun share(bitmap: Bitmap) {

        disposable.add(
            bitmap.toCachedPng(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { file ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(
                            Intent.EXTRA_STREAM,
                            FileProvider.getUriForFile(
                                this@MainActivity,
                                BuildConfig.APPLICATION_ID + ".provider",
                                file
                            )
                        )
                    }
                    startActivity(
                        Intent.createChooser(intent, "Share This Image with")
                    )
                }
        )
    }
}

private fun Bitmap.toCachedPng(context: Context): Single<File> {
    val cacheDir = context.externalCacheDir
    val fileName = System.currentTimeMillis().toString(16)

    val fileShare = File(cacheDir, "$fileName.png")

    FileOutputStream(fileShare).use { fos ->
        this.compress(Bitmap.CompressFormat.PNG, 100, fos)
    }
    return Single.just(fileShare)
}
