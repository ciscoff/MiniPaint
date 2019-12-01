package s.yarlykov.minipaint

import android.graphics.Bitmap

interface Interactor {
    fun share(bitmap: Bitmap)
    fun onDestroy()
}