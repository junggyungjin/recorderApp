package fastcampus_aop_part2.chapter07

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import kotlin.random.Random

class SoundVisualizerView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onRequestCurrentAmplitude: (() -> Int)? = null


    @RequiresApi(Build.VERSION_CODES.M)
    private val amplitudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.purple_500)
        strokeWidth = LINE_WIDTH
        strokeCap = Paint.Cap.ROUND
    }

    private var drawingWidth: Int = 0
    private var drawingHeight: Int = 0

    // 10번만큼 MAX_VALUE의 값중에서 랜덤값을 가져와서 리스트에 매핑해주는 코드
//    var drawingAmplitudes: List<Int> = (0..10).map { Random.nextInt(Short.MAX_VALUE.toInt()) }

    private var drawingAmplitudes: List<Int> = emptyList()
    private var isReplaying: Boolean = false
    private var replayingPosition: Int = 0


    private val visualizeRepeatAction: Runnable = object : Runnable {
        override fun run() {
            if (!isReplaying) {
                val currentAmplitude = onRequestCurrentAmplitude?.invoke() ?: 0

                // Amplitude
                drawingAmplitudes = listOf(currentAmplitude) + drawingAmplitudes
            } else {
                replayingPosition++
            }

            invalidate() // 데이터 갱신

            // Draw
            handler?.postDelayed(this, ACTION_INTERVAL)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingHeight = h
        drawingWidth = w
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // 캔버스가 null 일 경우 진행을 할 필요가 없음 그래서 바로 리턴
        canvas ?: return

        val centerY = drawingHeight / 2f

        //  시작포인트 잡기 - 그리는 영역에 가장 오른쪽 즉 가로 길이
        var offsetX = drawingWidth.toFloat()

        drawingAmplitudes
            .let { amplitudes ->
                if (isReplaying) {
                    amplitudes.takeLast(replayingPosition)
                }else {
                    amplitudes
                }
            }
            .forEach { amplitude ->
            val lineLenth = amplitude / MAX_AMPLITUDE * drawingHeight * 0.8F

            offsetX -= LINE_SPACE
            if (offsetX < 0) return@forEach // 뷰의 왼쪽영역보다 더 바깥쪽에 있다 그러면 더 이상 그리지 않아야 함

            canvas.drawLine(
                offsetX,

                centerY - lineLenth / 2F,
                offsetX,
                centerY + lineLenth / 2F,
                amplitudePaint
            )
        }

    }

    fun startVisualizing(isReplaying: Boolean) {
        this.isReplaying = isReplaying
        handler?.post(visualizeRepeatAction)
    }

    fun stopVisualizing() {
        replayingPosition = 0
        handler?.removeCallbacks(visualizeRepeatAction)
    }

    fun clearVisualization() {
        drawingAmplitudes = emptyList()
        invalidate()
    }

    companion object {
        private const val LINE_WIDTH = 10F
        private const val LINE_SPACE = 15F
        private const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat()
        private const val ACTION_INTERVAL = 20L
    }
}