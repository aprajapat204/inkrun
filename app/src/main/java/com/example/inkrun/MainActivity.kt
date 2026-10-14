package com.example.inkrun

import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.inkrun.core.Constants
import com.example.inkrun.core.GameLoop
import com.example.inkrun.data.SaveStore
import com.example.inkrun.entities.Player
import com.example.inkrun.input.TouchControls
import com.example.inkrun.render.Renderer
import com.example.inkrun.world.Level
import com.example.inkrun.world.LevelLoader
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), GameLoop.GameCallback, SurfaceHolder.Callback {

    private lateinit var gameView: GameSurfaceView
    private lateinit var touchControls: TouchControls
    private lateinit var gameLoop: GameLoop
    private lateinit var renderer: Renderer
    private lateinit var player: Player
    private lateinit var level: Level
    private lateinit var saveStore: SaveStore

    private var currentLevelPath = "levels/w01/l01_s1.txt"
    private var gameState = Renderer.GameState.PLAYING
    private var moveInput = 0f
    private var jumpPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen immersive mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Create game view
        gameView = GameSurfaceView(this)
        gameView.holder.addCallback(this)
        setContentView(gameView)

        // Touch controls overlay
        touchControls = TouchControls(this)
        touchControls.onMoveChanged = { move ->
            moveInput = move
        }
        touchControls.onJumpChanged = { pressed ->
            jumpPressed = pressed
        }
        addContentView(touchControls, android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        ))

        // Initialize game components
        saveStore = SaveStore(this)
        player = Player()
        gameLoop = GameLoop(gameView.holder, this)
        renderer = Renderer(0, 0) // Will be set in onSurfaceChanged

        // Load saved level
        lifecycleScope.launch {
            currentLevelPath = saveStore.getCurrentLevel()
            loadLevel(currentLevelPath)
        }

        // Initialize audio
        com.example.inkrun.audio.Tones.initialize()
    }

    private fun loadLevel(levelPath: String) {
        val loadedLevel = LevelLoader.loadLevel(this, levelPath)
        if (loadedLevel != null) {
            level = loadedLevel
            player.reset(level.spawnX, level.spawnY)
            gameState = Renderer.GameState.PLAYING
        } else {
            // Fallback to empty level
            level = Level.createEmpty(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT)
            player.reset(level.spawnX, level.spawnY)
        }
    }

    override fun onResume() {
        super.onResume()
        gameLoop.start()
    }

    override fun onPause() {
        super.onPause()
        gameLoop.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        com.example.inkrun.audio.Tones.release()
    }

    // GameLoop.GameCallback
    override fun update(deltaTime: Double) {
        when (gameState) {
            Renderer.GameState.PLAYING -> {
                // Update player
                player.update(deltaTime, moveInput, jumpPressed)

                // Collision with level
                val collision = level.checkCollision(
                    player.getLeft(), player.getRight(),
                    player.getBottom(), player.getTop()
                )
                player.resolveCollision(collision.first, collision.second, collision.third)

                // Hazard collision
                if (level.checkHazardCollision(
                        player.getLeft(), player.getRight(),
                        player.getBottom(), player.getTop()
                    )) {
                    gameState = Renderer.GameState.GAME_OVER
                    com.example.inkrun.audio.Tones.playGameOver()
                }

                // Goal collision
                if (level.checkGoalCollision(
                        player.getLeft(), player.getRight(),
                        player.getBottom(), player.getTop()
                    )) {
                    gameState = Renderer.GameState.LEVEL_COMPLETE
                    com.example.inkrun.audio.Tones.playLevelComplete()

                    // Auto-advance to next level after delay
                    // For now just complete
                }

                // Update camera
                renderer.updateCamera(player, level)
            }
            Renderer.GameState.GAME_OVER, Renderer.GameState.LEVEL_COMPLETE -> {
                // Wait for tap to restart/next
            }
        }
    }

    override fun render(canvas: android.graphics.Canvas) {
        renderer.render(canvas, player, level, gameState)
    }

    override fun onSurfaceCreated() {
        // Load level on surface creation
        loadLevel(currentLevelPath)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        renderer = Renderer(width, height)
        renderer.setScreenSize(width, height)
        gameLoop.onSurfaceChanged(width, height)
    }

    override fun onSurfaceDestroyed() {
        gameLoop.onSurfaceDestroyed()
    }

    // Touch handling for game state transitions
    override fun onTouchEvent(event: android.view.MotionEvent?): Boolean {
        if (event?.action == android.view.MotionEvent.ACTION_DOWN) {
            when (gameState) {
                Renderer.GameState.GAME_OVER -> {
                    loadLevel(currentLevelPath)
                }
                Renderer.GameState.LEVEL_COMPLETE -> {
                    // TODO: Load next level
                    loadLevel(currentLevelPath)
                }
                Renderer.GameState.PAUSED -> {
                    gameState = Renderer.GameState.PLAYING
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // SurfaceHolder.Callback
    override fun surfaceCreated(holder: SurfaceHolder) {
        gameLoop.onSurfaceCreated()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        gameLoop.onSurfaceChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        gameLoop.onSurfaceDestroyed()
    }

    inner class GameSurfaceView(context: android.content.Context) : SurfaceView(context) {
        override fun onWindowInsetChanged(insets: WindowInsets): WindowInsets {
            return insets
        }
    }
}