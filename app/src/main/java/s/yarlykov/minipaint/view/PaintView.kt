package s.yarlykov.minipaint.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import s.yarlykov.minipaint.model.PathStack
import s.yarlykov.minipaint.R

private const val STROKE_WIDTH = 12f

/**
 * Экран рисования
 */
class PaintView(context: Context,
                colors : Pair<Int, Int>,
                private val pathStack: PathStack) : View(context) {

    /**
     * Все рисование делаем в отдельной битмапе. Потом в onDraw()
     * копируем её контент в битмапу нашей View.
     * @cacheBitmap
     * @cacheCanvas
     */
    private lateinit var cacheBitmap: Bitmap
    private lateinit var cacheCanvas: Canvas

    var colorBackground = colors.first
        private set

    var colorDraw = colors.second
        private set

    // Current Path
    private var curPath = Path()

    // Расстояние в рх. Если палец сместился на это расстояние, то считается как скролл (или move)
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    // Положение пальца на последнем TouchEvent
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    // Положение пальца на предыдущем TouchEvent
    private var currentX = 0f
    private var currentY = 0f

    private val paint = Paint().apply {
        color = colorDraw

        // Смягчить по краям
        isAntiAlias = true
        // Уменьшить последствия искажения цвета на "дешевых" девайсах
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth =
            STROKE_WIDTH // default: Hairline-width (really thin)
    }

    init {
        id = System.currentTimeMillis().toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Чтобы не было утечки памяти, удалить старую битмапу перед созданием новой
        if (::cacheBitmap.isInitialized) cacheBitmap.recycle()

        cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        cacheCanvas = Canvas(cacheBitmap)
        cacheCanvas.drawColor(colorBackground)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(cacheBitmap, 0f, 0f, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Сохранить координаты текущего события
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private fun touchStart() {
        curPath.reset()
        curPath.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)

        if (dx >= touchTolerance || dy >= touchTolerance) {
            // Добавить фрагмент пути.
            // Это квадратичная кривая бизье от предудыщего положения пальца до текущего
            // с "изгибом" в сторону точки (x2,y2). Можно использовать и lineTo(), но
            // с quadTo() линия будет плавнее.
            curPath.quadTo(
                currentX, currentY,
                (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2
            )
            currentX = motionTouchEventX
            currentY = motionTouchEventY

            for (p in pathStack) {
                cacheCanvas.drawPath(p, paint)
            }

            cacheCanvas.drawPath(curPath, paint)
        }
        invalidate()
    }

    private fun touchUp() {

        if (!curPath.isEmpty) {
            pathStack.push(Path(curPath))
        }

        curPath.reset()
    }

    /**
     * Используется для шаринга
     */
    fun getBitmap(): Bitmap = cacheBitmap

    fun onDataChanged() {
        cacheCanvas.drawColor(colorBackground)

        for (p in pathStack) {
            cacheCanvas.drawPath(p, paint)
        }
        invalidate()
    }

    /**
     * Если изменились цвета, то их нужно сохранять в глобальных полях, потому что
     * к ним нужен доступ из MainActivity.
     */
    fun onColorsChanged(bg: Int, fg: Int) {
        colorBackground = bg
        colorDraw = fg

        paint.color = colorDraw
        onDataChanged()
    }
}