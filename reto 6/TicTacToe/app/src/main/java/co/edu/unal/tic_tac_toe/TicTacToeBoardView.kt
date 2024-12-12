package co.edu.unal.tic_tac_toe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat

class TicTacToeBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val board = Array(3) { Array(3) { "" } }
    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val xDrawable = ResourcesCompat.getDrawable(resources, R.drawable.x_image, null)
    private val oDrawable = ResourcesCompat.getDrawable(resources, R.drawable.o_image, null)

    var onCellClick: ((Int, Int) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cellSize = width / 3f

        // Dibujar las líneas del tablero
        for (i in 1..2) {
            canvas.drawLine(cellSize * i, 0f, cellSize * i, height.toFloat(), paint)
            canvas.drawLine(0f, cellSize * i, width.toFloat(), cellSize * i, paint)
        }

        // Dibujar las marcas en las celdas
        for (i in 0..2) {
            for (j in 0..2) {
                val left = j * cellSize
                val top = i * cellSize
                val rect = RectF(left, top, left + cellSize, top + cellSize)

                when (board[i][j]) {
                    "X" -> xDrawable?.setBounds(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())
                        ?.also { xDrawable.draw(canvas) }
                    "O" -> oDrawable?.setBounds(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())
                        ?.also { oDrawable.draw(canvas) }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val cellSize = width / 3f
            val row = (event.y / cellSize).toInt()
            val col = (event.x / cellSize).toInt()
            if (row in 0..2 && col in 0..2) {
                onCellClick?.invoke(row, col)
            }
            return true
        }
        return false
    }

    fun updateBoard(newBoard: Array<Array<String>>) {
        for (i in 0..2) {
            for (j in 0..2) {
                board[i][j] = newBoard[i][j]
            }
        }
        invalidate()
    }
}