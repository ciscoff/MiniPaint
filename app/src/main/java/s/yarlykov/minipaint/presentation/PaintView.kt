package s.yarlykov.minipaint.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import s.yarlykov.minipaint.R

private const val STROKE_WIDTH = 12f
private const val DOUBLE_CLICK_INTERVAL = 200L

class PaintView(context: Context) : View(context) {

    lateinit var cacheBitmap: Bitmap
    lateinit var cacheCanvas: Canvas

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)
    private var millPrev = System.currentTimeMillis()
    private var path = Path()

    // Расстояние в рх. Если палец сместился на это расстояние, то считается как скролл (или move)
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    // Положение пальца на последнем TouchEvent
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    // Положение пальца на предыдущем TouchEvent
    private var currentX = 0f
    private var currentY = 0f

    val paint = Paint().apply {
        color = drawColor

        // Смягчить по краям
        isAntiAlias = true
        // Уменьшить последствия искажения цвета на "дешевых" девайсах
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Чтобы не было утечки памяти, удалить старую битмапу перед созданием новой
        if (::cacheBitmap.isInitialized) cacheBitmap.recycle()

        cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        cacheCanvas = Canvas(cacheBitmap)

        cacheCanvas.drawColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(cacheBitmap, 0f, 0f, null)
    }

    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
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
            path.quadTo(
                currentX, currentY,
                (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2
            )
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            cacheCanvas.drawPath(path, paint)
        }
        invalidate()
    }

    private fun touchUp() {
        val millsCurrent = System.currentTimeMillis()

        if(millsCurrent - millPrev <= DOUBLE_CLICK_INTERVAL) {
            resetAll()
        }

        millPrev = millsCurrent
        path.reset()
    }

    fun getBitmap() : Bitmap = cacheBitmap

    fun resetAll() {
        motionTouchEventX = 0f
        motionTouchEventY = 0f
        currentX = 0f
        currentY = 0f
        path.reset()

        cacheCanvas.drawColor(backgroundColor)
        invalidate()
    }
}