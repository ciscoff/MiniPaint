package s.yarlykov.minipaint.view

import android.view.View
import s.yarlykov.minipaint.view.custom.ColorView

interface ChoiceHandler {
    fun onPreviewCreated(bgView : View, fgView : View)
    fun onColorClicked(view : ColorView)
}