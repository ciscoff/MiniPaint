package s.yarlykov.minipaint.domain

import android.graphics.Bitmap

interface Interactor {
    fun share(bitmap: Bitmap)
    fun onDestroy()
}