package s.yarlykov.minipaint

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream

private const val TEMPORARY_FILE_NAME = "bitmap_tmp"

class InteractorImpl(private var context: Context?) : Interactor {

    override fun share(bitmap: Bitmap) {

        bitmapToPng(bitmap)?.let { single ->
            single
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { file ->
                    context?.let {ctx ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/*"
                            putExtra(
                                Intent.EXTRA_STREAM,
                                FileProvider.getUriForFile(ctx,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    file)
                            )
                        }

                        ctx.startActivity(
                            Intent.createChooser(intent, "Share This Image with")
                        )
                    }
                }
        }
    }

    override fun onDestroy() {
        context = null
    }

    private fun bitmapToPng(bitmap: Bitmap, fileName: String = TEMPORARY_FILE_NAME): Single<File>? {

        context?.let { ctx ->
            val cacheDir = ctx.externalCacheDir
            val fileShare = File(cacheDir, "$fileName.png")

            FileOutputStream(fileShare).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            return Single.just(fileShare)
        }
        return null
    }
}