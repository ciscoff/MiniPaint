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
import androidx.core.content.res.ResourcesCompat
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import s.yarlykov.minipaint.BuildConfig
import s.yarlykov.minipaint.view.PaletteActivity
import s.yarlykov.minipaint.model.PathStack
import s.yarlykov.minipaint.R
import s.yarlykov.minipaint.view.PaintView
import java.io.File
import java.io.FileOutputStream

private const val REQUEST_COLOR = 1

class MainActivity : AppCompatActivity() {

    /**
     * @paintView - Экран рисования
     * @pathStack - Стэк (он же List) для истории нарисованных линий
     *
     */
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

    /**
     * Запустить активити ColorPicker'а. Передать в неё текущие цветовые настройки экрана
     * рисования. Нужно, чтобы внутри ColorPicker'а в элементе preview показать текущие
     * цвета фона и кисти.
     */
    private fun startPaletteActivity() {
        val intent = Intent(this, PaletteActivity::class.java).apply {
            putExtra(getString(R.string.key_bg), paintView.colorBackground)
            putExtra(getString(R.string.key_fg), paintView.colorDraw)
        }
        startActivityForResult(intent, REQUEST_COLOR)
    }

    /**
     * Из активити ColorPicker'а должны прилететь новые настройки цвета, которые
     * передаем в paintView вызовом onColorsChanged()
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode != REQUEST_COLOR) return

        data?.let {intent ->

            paintView.onColorsChanged(
                intent.getIntExtra(getString(R.string.key_bg),
                    ResourcesCompat.getColor(resources, R.color.colorBackground, null)),
                intent.getIntExtra(getString(R.string.key_fg),
                    ResourcesCompat.getColor(resources, R.color.colorPaint, null))
            )
        }
    }

    /**
     * Открыть системный chooser для выбора приложения отправки картинки
     */
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

/**
 * Сохранить битмапу в файл в кэш-каталоге приложения и вернуть этот файл через Single<File>
 */
private fun Bitmap.toCachedPng(context: Context): Single<File> {
    val cacheDir = context.externalCacheDir
    val fileName = System.currentTimeMillis().toString(16)

    val fileShare = File(cacheDir, "$fileName.png")

    FileOutputStream(fileShare).use { fos ->
        this.compress(Bitmap.CompressFormat.PNG, 100, fos)
    }
    return Single.just(fileShare)
}
