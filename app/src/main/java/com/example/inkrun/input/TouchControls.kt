package com.example.inkrun.input

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import com.example.inkrun.core.Constants

class TouchControls(context: Context) : View(context) {

    private val buttonPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFFFFFFFF.toInt()
        alpha = (Constants.BUTTON_ALPHA * 255).toInt()
        style = Paint.Style.FILL
    }

    private val buttonPressedPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFFFFFFFF.toInt()
        alpha = (Constants.BUTTON_PRESSED_ALPHA * 255).toInt()
        style = Paint.Style.FILL
    }

    private val buttonBorderPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF000000.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val arrowPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF000000.toInt()
        style = Paint.Style.FILL
    }

    // Button bounds in screen pixels
    private val leftButton = RectF()
    private val rightButton = RectF()
    private val jumpButton = RectF()

    // Touch state
    private var leftPressed = false
    private var rightPressed = false
    private var jumpPressed = false
    private var leftPointerId = -1
    private var rightPointerId = -1
    private var jumpPointerId = -1

    // Callbacks
    var onMoveChanged: ((Float) -> Unit)? = null
    var onJumpChanged: ((Boolean) -> Unit)? = null

    private val buttonSizePx: Int
    private val buttonMarginPx: Int

    init {
        val metrics = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager)
            .defaultDisplay.getMetrics(metrics)

        buttonSizePx = (Constants.BUTTON_SIZE_DP * metrics.density).toInt()
        buttonMarginPx = (Constants.BUTTON_MARGIN_DP * metrics.density).toInt()

        setWillNotDraw(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Left button (bottom-left)
        leftButton.set(
            buttonMarginPx.toFloat(),
            (h - buttonSizePx - buttonMarginPx).toFloat(),
            (buttonMarginPx + buttonSizePx).toFloat(),
            (h - buttonMarginPx).toFloat()
        )

        // Right button (next to left)
        rightButton.set(
            (buttonMarginPx + buttonSizePx + buttonMarginPx).toFloat(),
            (h - buttonSizePx - buttonMarginPx).toFloat(),
            (buttonMarginPx * 2 + buttonSizePx * 2).toFloat(),
            (h - buttonMarginPx).toFloat()
        )

        // Jump button (bottom-right)
        jumpButton.set(
            (w - buttonSizePx - buttonMarginPx).toFloat(),
            (h - buttonSizePx - buttonMarginPx).toFloat(),
            (w - buttonMarginPx).toFloat(),
            (h - buttonMarginPx).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw left button
        drawButton(canvas, leftButton, leftPressed, -1)

        // Draw right button
        drawButton(canvas, rightButton, rightPressed, 1)

        // Draw jump button
        drawButton(canvas, jumpButton, jumpPressed, 0)
    }

    private fun drawButton(canvas: Canvas, button: RectF, pressed: Boolean, direction: Int) {
        val paint = if (pressed) buttonPressedPaint else buttonPaint
        canvas.drawRoundRect(button, 16f, 16f, paint)
        canvas.drawRoundRect(button, 16f, 16f, buttonBorderPaint)

        // Draw arrow/jump symbol
        val cx = button.centerX()
        val cy = button.centerY()
        val arrowSize = buttonSizePx * 0.3f

        when (direction) {
            -1 -> { // Left arrow
                canvas.drawPath(
                    android.graphics.Path().apply {
                        moveTo(cx + arrowSize * 0.5f, cy)
                        lineTo(cx - arrowSize * 0.5f, cy - arrowSize * 0.5f)
                        lineTo(cx - arrowSize * 0.5f, cy + arrowSize * 0.5f)
                        close()
                    },
                    arrowPaint
                )
            }
            1 -> { // Right arrow
                canvas.drawPath(
                    android.graphics.Path().apply {
                        moveTo(cx - arrowSize * 0.5f, cy)
                        lineTo(cx + arrowSize * 0.5f, cy - arrowSize * 0.5f)
                        lineTo(cx + arrowSize * 0.5f, cy + arrowSize * 0.5f)
                        close()
                    },
                    arrowPaint
                )
            }
            0 -> { // Jump (up arrow)
                canvas.drawPath(
                    android.graphics.Path().apply {
                        moveTo(cx, cy - arrowSize * 0.5f)
                        lineTo(cx - arrowSize * 0.5f, cy + arrowSize * 0.5f)
                        lineTo(cx + arrowSize * 0.5f, cy + arrowSize * 0.5f)
                        close()
                    },
                    arrowPaint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)

        when (action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN -> {
                handlePointerDown(pointerId, x, y)
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {
                handlePointerUp(pointerId)
            }
            MotionEvent.ACTION_MOVE -> {
                handlePointerMove(event)
            }
        }
        return true
    }

    private fun handlePointerDown(pointerId: Int, x: Float, y: Float) {
        if (leftButton.contains(x, y)) {
            leftPressed = true
            leftPointerId = pointerId
            onMoveChanged?.invoke(-1f)
        } else if (rightButton.contains(x, y)) {
            rightPressed = true
            rightPointerId = pointerId
            onMoveChanged?.invoke(1f)
        } else if (jumpButton.contains(x, y)) {
            jumpPressed = true
            jumpPointerId = pointerId
            onJumpChanged?.invoke(true)
        }
        invalidate()
    }

    private fun handlePointerUp(pointerId: Int) {
        var moveChanged = false

        if (pointerId == leftPointerId) {
            leftPressed = false
            leftPointerId = -1
            moveChanged = true
        } else if (pointerId == rightPointerId) {
            rightPressed = false
            rightPointerId = -1
            moveChanged = true
        } else if (pointerId == jumpPointerId) {
            jumpPressed = false
            jumpPointerId = -1
            onJumpChanged?.invoke(false)
        }

        if (moveChanged) {
            val move = when {
                leftPressed -> -1f
                rightPressed -> 1f
                else -> 0f
            }
            onMoveChanged?.invoke(move)
        }
        invalidate()
    }

    private fun handlePointerMove(event: MotionEvent) {
        var moveChanged = false

        // Check left pointer
        if (leftPointerId != -1) {
            val index = event.findPointerIndex(leftPointerId)
            if (index >= 0) {
                val x = event.getX(index)
                val y = event.getY(index)
                if (!leftButton.contains(x, y)) {
                    leftPressed = false
                    leftPointerId = -1
                    moveChanged = true
                }
            }
        }

        // Check right pointer
        if (rightPointerId != -1) {
            val index = event.findPointerIndex(rightPointerId)
            if (index >= 0) {
                val x = event.getX(index)
                val y = event.getY(index)
                if (!rightButton.contains(x, y)) {
                    rightPressed = false
                    rightPointerId = -1
                    moveChanged = true
                }
            }
        }

        // Check jump pointer
        if (jumpPointerId != -1) {
            val index = event.findPointerIndex(jumpPointerId)
            if (index >= 0) {
                val x = event.getX(index)
                val y = event.getY(index)
                if (!jumpButton.contains(x, y)) {
                    jumpPressed = false
                    jumpPointerId = -1
                    onJumpChanged?.invoke(false)
                }
            }
        }

        if (moveChanged) {
            val move = when {
                leftPressed -> -1f
                rightPressed -> 1f
                else -> 0f
            }
            onMoveChanged?.invoke(move)
        }
        invalidate()
    }

    fun getMoveInput(): Float {
        return when {
            leftPressed && rightPressed -> 0f
            leftPressed -> -1f
            rightPressed -> 1f
            else -> 0f
        }
    }

    fun isJumpPressed(): Boolean = jumpPressed
}