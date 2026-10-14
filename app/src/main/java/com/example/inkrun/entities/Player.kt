package com.example.inkrun.entities

import com.example.inkrun.core.Constants

class Player {
    var x = 0f
    var y = 0f
    var velocityX = 0f
    var velocityY = 0f
    var onGround = false
    var facingRight = true

    // Coyote time & jump buffer
    private var coyoteTimer = 0f
    private var jumpBufferTimer = 0f
    private var jumpPressed = false
    private var moveInput = 0f

    // State
    var alive = true
    var finished = false

    fun update(deltaTime: Double, moveInput: Float, jumpPressed: Boolean) {
        this.moveInput = moveInput
        this.jumpPressed = jumpPressed

        val dt = deltaTime.toFloat()

        // Update coyote time
        if (onGround) {
            coyoteTimer = Constants.PLAYER_COYOTE_TIME
        } else {
            coyoteTimer -= dt
        }

        // Update jump buffer
        if (jumpPressed) {
            jumpBufferTimer = Constants.PLAYER_JUMP_BUFFER
        } else {
            jumpBufferTimer -= dt
        }

        // Horizontal movement
        velocityX = moveInput * Constants.PLAYER_SPEED

        // Jump logic
        if (jumpBufferTimer > 0 && coyoteTimer > 0) {
            velocityY = Constants.PLAYER_JUMP_VELOCITY
            jumpBufferTimer = 0f
            coyoteTimer = 0f
            onGround = false
        }

        // Apply gravity
        velocityY -= Constants.PLAYER_GRAVITY * dt

        // Clamp fall speed
        if (velocityY < -Constants.PLAYER_MAX_FALL_SPEED) {
            velocityY = -Constants.PLAYER_MAX_FALL_SPEED
        }

        // Update position
        x += velocityX * dt
        y += velocityY * dt

        // Update facing direction
        if (moveInput > 0) facingRight = true
        else if (moveInput < 0) facingRight = false
    }

    fun resolveCollision(newX: Float, newY: Float, hitGround: Boolean) {
        x = newX
        y = newY
        if (hitGround) {
            onGround = true
            velocityY = 0f
        }
    }

    fun reset(spawnX: Float, spawnY: Float) {
        x = spawnX
        y = spawnY
        velocityX = 0f
        velocityY = 0f
        onGround = false
        coyoteTimer = 0f
        jumpBufferTimer = 0f
        alive = true
        finished = false
        facingRight = true
    }

    fun getLeft() = x - Constants.PLAYER_WIDTH / 2
    fun getRight() = x + Constants.PLAYER_WIDTH / 2
    fun getTop() = y + Constants.PLAYER_HEIGHT / 2
    fun getBottom() = y - Constants.PLAYER_HEIGHT / 2
}