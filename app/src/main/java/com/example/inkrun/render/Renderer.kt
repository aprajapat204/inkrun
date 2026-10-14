package com.example.inkrun.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.inkrun.core.Constants
import com.example.inkrun.entities.Player
import com.example.inkrun.world.Level

class Renderer(private val screenWidth: Int, private val screenHeight: Int) {

    // Paint objects (reused to avoid allocations)
    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val tilePaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF444444.toInt()
        style = Paint.Style.FILL
    }

    private val tileBorderPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF666666.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val playerPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF00AAFF.toInt()
        style = Paint.Style.FILL
    }

    private val playerOutlinePaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF0066AA.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val goalPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF00FF00.toInt()
        style = Paint.Style.FILL
    }

    private val hazardPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFFFF0000.toInt()
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }

    private val uiPaint = Paint().apply {
        isAntiAlias = true
        color = 0xAA000000.toInt()
        style = Paint.Style.FILL
    }

    // Camera
    private var cameraX = 0f
    private var cameraY = 0f

    // World to screen conversion
    private var worldToScreenX = 0f
    private var worldToScreenY = 0f
    private var screenToWorldX = 0f
    private var screenToWorldY = 0f

    private val tempRect = RectF()
    private val tempRect2 = RectF()

    fun setScreenSize(width: Int, height: Int) {
        // Calculate scale to fit world in screen maintaining aspect ratio
        val scaleX = width / Constants.WORLD_WIDTH
        val scaleY = height / Constants.WORLD_HEIGHT
        val scale = minOf(scaleX, scaleY)

        worldToScreenX = scale
        worldToScreenY = scale
        screenToWorldX = 1f / scale
        screenToWorldY = 1f / scale

        // Center camera
        cameraX = Constants.WORLD_WIDTH / 2
        cameraY = Constants.WORLD_HEIGHT / 2
    }

    fun updateCamera(player: Player, level: Level) {
        // Smooth camera follow with deadzone
        val targetX = player.x
        val targetY = player.y

        val dx = targetX - cameraX
        val dy = targetY - cameraY

        if (abs(dx) > Constants.CAMERA_DEADZONE_X) {
            cameraX += dx * Constants.CAMERA_LERP * Constants.FIXED_TIME_STEP
        }
        if (abs(dy) > Constants.CAMERA_DEADZONE_Y) {
            cameraY += dy * Constants.CAMERA_LERP * Constants.FIXED_TIME_STEP
        }

        // Clamp camera to level bounds
        val halfWorldW = (screenWidth * screenToWorldX) / 2
        val halfWorldH = (screenHeight * screenToWorldY) / 2

        cameraX = cameraX.coerceIn(halfWorldW, level.width - halfWorldW)
        cameraY = cameraY.coerceIn(halfWorldH, level.height - halfWorldH)
    }

    fun render(canvas: Canvas, player: Player, level: Level, gameState: GameState) {
        // Clear
        canvas.drawColor(Color.BLACK)

        // Calculate visible world bounds
        val halfWorldW = (screenWidth * screenToWorldX) / 2
        val halfWorldH = (screenHeight * screenToWorldY) / 2
        val left = cameraX - halfWorldW
        val right = cameraX + halfWorldW
        val bottom = cameraY - halfWorldH
        val top = cameraY + halfWorldH

        // Draw level tiles
        drawLevel(canvas, level, left, right, bottom, top)

        // Draw goal
        drawGoal(canvas, level)

        // Draw hazards
        drawHazards(canvas, level, left, right, bottom, top)

        // Draw player
        drawPlayer(canvas, player)

        // Draw UI
        drawUI(canvas, gameState)
    }

    private fun drawLevel(canvas: Canvas, level: Level, left: Float, right: Float, bottom: Float, top: Float) {
        val tileSize = Constants.TILE_SIZE
        val startX = max(0, floor(left / tileSize).toInt())
        val endX = min(level.widthTiles - 1, ceil(right / tileSize).toInt())
        val startY = max(0, floor(bottom / tileSize).toInt())
        val endY = min(level.heightTiles - 1, ceil(top / tileSize).toInt())

        for (y in startY..endY) {
            for (x in startX..endX) {
                if (level.tiles[y][x] == 1) {
                    val screenX = (x * tileSize - cameraX) * worldToScreenX + screenWidth / 2f
                    val screenY = (cameraY - y * tileSize) * worldToScreenY + screenHeight / 2f
                    val screenTileSize = tileSize * worldToScreenX

                    tempRect.set(
                        screenX,
                        screenY - screenTileSize,
                        screenX + screenTileSize,
                        screenY
                    )
                    canvas.drawRect(tempRect, tilePaint)
                    canvas.drawRect(tempRect, tileBorderPaint)
                }
            }
        }
    }

    private fun drawGoal(canvas: Canvas, level: Level) {
        val screenX = (level.goalX - cameraX) * worldToScreenX + screenWidth / 2f
        val screenY = (cameraY - level.goalY) * worldToScreenY + screenHeight / 2f
        val size = Constants.TILE_SIZE * worldToScreenX * 1.5f

        tempRect.set(
            screenX - size / 2,
            screenY - size / 2,
            screenX + size / 2,
            screenY + size / 2
        )
        canvas.drawRect(tempRect, goalPaint)
    }

    private fun drawHazards(canvas: Canvas, level: Level, left: Float, right: Float, bottom: Float, top: Float) {
        for (hazard in level.hazards) {
            if (hazard.x + hazard.width > left && hazard.x < right &&
                hazard.y + hazard.height > bottom && hazard.y < top) {

                val screenX = (hazard.x - cameraX) * worldToScreenX + screenWidth / 2f
                val screenY = (cameraY - hazard.y) * worldToScreenY + screenHeight / 2f
                val screenW = hazard.width * worldToScreenX
                val screenH = hazard.height * worldToScreenY

                tempRect.set(
                    screenX,
                    screenY - screenH,
                    screenX + screenW,
                    screenY
                )
                canvas.drawRect(tempRect, hazardPaint)
            }
        }
    }

    private fun drawPlayer(canvas: Canvas, player: Player) {
        val screenX = (player.x - cameraX) * worldToScreenX + screenWidth / 2f
        val screenY = (cameraY - player.y) * worldToScreenY + screenHeight / 2f
        val screenW = Constants.PLAYER_WIDTH * worldToScreenX
        val screenH = Constants.PLAYER_HEIGHT * worldToScreenY

        tempRect.set(
            screenX - screenW / 2,
            screenY - screenH / 2,
            screenX + screenW / 2,
            screenY + screenH / 2
        )
        canvas.drawRect(tempRect, playerPaint)
        canvas.drawRect(tempRect, playerOutlinePaint)

        // Draw facing direction indicator
        val eyeX = screenX + (if (player.facingRight) screenW * 0.25f else -screenW * 0.25f)
        val eyeY = screenY + screenH * 0.2f
        val eyeSize = screenW * 0.15f
        canvas.drawCircle(eyeX, eyeY, eyeSize, Paint().apply { color = Color.WHITE })
    }

    private fun drawUI(canvas: Canvas, gameState: GameState) {
        when (gameState) {
            GameState.PLAYING -> {
                // No UI during play
            }
            GameState.PAUSED -> {
                drawCenteredText(canvas, "PAUSED", screenHeight / 2f - 50)
                drawCenteredText(canvas, "Tap to Resume", screenHeight / 2f + 50)
            }
            GameState.LEVEL_COMPLETE -> {
                drawCenteredText(canvas, "LEVEL COMPLETE!", screenHeight / 2f - 50)
                drawCenteredText(canvas, "Tap for Next Level", screenHeight / 2f + 50)
            }
            GameState.GAME_OVER -> {
                drawCenteredText(canvas, "GAME OVER", screenHeight / 2f - 50)
                drawCenteredText(canvas, "Tap to Retry", screenHeight / 2f + 50)
            }
        }
    }

    private fun drawCenteredText(canvas: Canvas, text: String, y: Float) {
        // Background
        val bounds = android.graphics.Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        tempRect2.set(
            screenWidth / 2f - bounds.width() / 2f - 20,
            y - bounds.height() - 10,
            screenWidth / 2f + bounds.width() / 2f + 20,
            y + 10
        )
        canvas.drawRoundRect(tempRect2, 10f, 10f, uiPaint)
        canvas.drawText(text, screenWidth / 2f, y, textPaint)
    }

    enum class GameState {
        PLAYING, PAUSED, LEVEL_COMPLETE, GAME_OVER
    }
}