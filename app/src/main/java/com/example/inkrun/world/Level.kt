package com.example.inkrun.world

import android.util.Pair

data class Level(
    val width: Float,
    val height: Float,
    val widthTiles: Int,
    val heightTiles: Int,
    val tiles: Array<IntArray>, // 0 = empty, 1 = solid
    val spawnX: Float,
    val spawnY: Float,
    val goalX: Float,
    val goalY: Float,
    val hazards: List<Hazard>
) {
    data class Hazard(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float
    )

    fun isSolid(tileX: Int, tileY: Int): Boolean {
        return tileX in 0 until widthTiles && tileY in 0 until heightTiles && tiles[tileY][tileX] == 1
    }

    fun checkCollision(entityLeft: Float, entityRight: Float, entityBottom: Float, entityTop: Float): Pair<Float, Float> {
        var newX = entityLeft
        var newY = entityBottom
        var hitGround = false

        val tileSize = 1f
        val startX = max(0, floor(entityLeft / tileSize).toInt())
        val endX = min(widthTiles - 1, floor(entityRight / tileSize).toInt())
        val startY = max(0, floor(entityBottom / tileSize).toInt())
        val endY = min(heightTiles - 1, floor(entityTop / tileSize).toInt())

        for (y in startY..endY) {
            for (x in startX..endX) {
                if (isSolid(x, y)) {
                    val tileLeft = x * tileSize
                    val tileRight = (x + 1) * tileSize
                    val tileBottom = y * tileSize
                    val tileTop = (y + 1) * tileSize

                    // Calculate overlap
                    val overlapX = min(entityRight, tileRight) - max(entityLeft, tileLeft)
                    val overlapY = min(entityTop, tileTop) - max(entityBottom, tileBottom)

                    if (overlapX < overlapY) {
                        // Horizontal collision
                        if (entityRight > tileLeft && entityLeft < tileLeft) {
                            newX = tileLeft - (entityRight - entityLeft)
                        } else if (entityLeft < tileRight && entityRight > tileRight) {
                            newX = tileRight
                        }
                    } else {
                        // Vertical collision
                        if (entityTop > tileBottom && entityBottom < tileBottom) {
                            newY = tileBottom - (entityTop - entityBottom)
                            hitGround = true
                        } else if (entityBottom < tileTop && entityTop > tileTop) {
                            newY = tileTop
                        }
                    }
                }
            }
        }

        return Pair(newX + (entityRight - entityLeft) / 2, newY + (entityTop - entityBottom) / 2, hitGround)
    }

    fun checkHazardCollision(entityLeft: Float, entityRight: Float, entityBottom: Float, entityTop: Float): Boolean {
        for (hazard in hazards) {
            if (entityRight > hazard.x && entityLeft < hazard.x + hazard.width &&
                entityTop > hazard.y && entityBottom < hazard.y + hazard.height) {
                return true
            }
        }
        return false
    }

    fun checkGoalCollision(entityLeft: Float, entityRight: Float, entityBottom: Float, entityTop: Float): Boolean {
        val goalRadius = 1f
        return entityRight > goalX - goalRadius && entityLeft < goalX + goalRadius &&
               entityTop > goalY - goalRadius && entityBottom < goalY + goalRadius
    }

    companion object {
        fun createEmpty(width: Float, height: Float): Level {
            val widthTiles = width.toInt()
            val heightTiles = height.toInt()
            val tiles = Array(heightTiles) { IntArray(widthTiles) { 0 } }

            // Add floor
            for (x in 0 until widthTiles) {
                tiles[0][x] = 1
            }

            return Level(
                width = width,
                height = height,
                widthTiles = widthTiles,
                heightTiles = heightTiles,
                tiles = tiles,
                spawnX = 2f,
                spawnY = 2f,
                goalX = width - 2f,
                goalY = 2f,
                hazards = emptyList()
            )
        }
    }
}