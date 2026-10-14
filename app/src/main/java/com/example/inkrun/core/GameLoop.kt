package com.example.inkrun.core

import android.graphics.Canvas
import android.view.SurfaceHolder

class GameLoop(
    private val surfaceHolder: SurfaceHolder,
    private val gameCallback: GameCallback
) : Runnable {

    private var running = false
    private var thread: Thread? = null

    // Fixed timestep variables
    private var lastTime = 0L
    private var accumulator = 0.0
    private val fixedTimeStep = Constants.FIXED_TIME_STEP
    private val maxFrameSkip = Constants.MAX_FRAME_SKIP

    interface GameCallback {
        fun update(deltaTime: Double)
        fun render(canvas: Canvas)
        fun onSurfaceCreated()
        fun onSurfaceChanged(width: Int, height: Int)
        fun onSurfaceDestroyed()
    }

    fun start() {
        if (running) return
        running = true
        thread = Thread(this).apply {
            name = "GameLoopThread"
            start()
        }
    }

    fun stop() {
        running = false
        thread?.interrupt()
        thread?.join()
        thread = null
    }

    override fun run() {
        lastTime = System.nanoTime()

        while (running) {
            val currentTime = System.nanoTime()
            val frameTime = (currentTime - lastTime) / 1_000_000_000.0
            lastTime = currentTime

            accumulator += frameTime

            var framesSkipped = 0
            while (accumulator >= fixedTimeStep && framesSkipped < maxFrameSkip) {
                gameCallback.update(fixedTimeStep)
                accumulator -= fixedTimeStep
                framesSkipped++
            }

            // Render
            val canvas = surfaceHolder.lockCanvas()
            if (canvas != null) {
                try {
                    gameCallback.render(canvas)
                } finally {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }

            // Small sleep to prevent CPU spinning
            Thread.sleep(1)
        }
    }

    fun onSurfaceCreated() {
        gameCallback.onSurfaceCreated()
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        gameCallback.onSurfaceChanged(width, height)
    }

    fun onSurfaceDestroyed() {
        gameCallback.onSurfaceDestroyed()
    }
}