package com.example.camera.utils.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Size
import android.widget.FrameLayout
import com.example.camera.R
import com.example.camera.detection.Recognition
import com.example.camera.presentation.main.TrackingBox
import com.example.camera.utils.ImageUtils
import com.example.camera.utils.spToPx
import com.example.camera.utils.withColor
import kotlin.math.min

class TrackerOverlayView(context: Context, attributeSet: AttributeSet?) : FrameLayout(context, attributeSet) {

    companion object {
        private const val TEXT_SIZE_SP = 12
        private const val BOX_STROKE_WIDTH = 10f
        private const val TEXT_BACKGROUND_ALPHA = 160
    }

    private var availableColors: IntArray = resources.getIntArray(R.array.bbox_colors)
    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = BOX_STROKE_WIDTH
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textBackgroundPaint = Paint().apply {
        alpha = TEXT_BACKGROUND_ALPHA
        style = Paint.Style.FILL
    }
    private val textPaint: Paint = Paint().apply {
        alpha = TEXT_BACKGROUND_ALPHA
        textSize = spToPx(TEXT_SIZE_SP)
        color = Color.WHITE
        typeface = Typeface.MONOSPACE
    }

    private lateinit var boxes: List<TrackingBox>
    private lateinit var modelOutputToCanvasMatrix: Matrix

    init {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (this::boxes.isInitialized) {
            boxes.forEach { box ->
                val cornerSize = min(box.location.width(), box.location.height()) / 8f
                canvas.drawRoundRect(box.location, cornerSize, cornerSize, boxPaint.withColor(box.color))
                canvas.drawBorderedText(
                    x = box.location.left + cornerSize,
                    y = box.location.top,
                    height = textPaint.textSize,
                    width = textPaint.measureText(box.description),
                    color = box.color,
                    text = box.description
                )
            }
        }
    }

    private fun Canvas.drawBorderedText(x: Float, y: Float, width: Float, height: Float, color: Int, text: String) {
        drawRect(x, y, x + width, y + height, textBackgroundPaint.withColor(color))
        drawText(text, x, y + height - 10, textPaint)
    }

    fun setModelInputSize(outputSize: Size?) {
        outputSize ?: return
        modelOutputToCanvasMatrix =
            ImageUtils.getTransformationMatrix(
                outputSize.width, outputSize.height,
                width, height
            )
    }

    fun setData(list: List<Recognition>) {
        if (!this::modelOutputToCanvasMatrix.isInitialized) return

        boxes = list.mapIndexed { index, recognition ->
            val mappedLocation = RectF(recognition.location)
            modelOutputToCanvasMatrix.mapRect(mappedLocation)

            TrackingBox(
                location = mappedLocation,
                confidence = recognition.confidence,
                title = recognition.title,
                color = availableColors[index]
            )
        }

        invalidate()
    }
}
