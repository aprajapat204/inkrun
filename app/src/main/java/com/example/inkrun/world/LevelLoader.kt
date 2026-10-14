package com.example.inkrun.world

import android.content.Context
import android.util.Log
import com.example.inkrun.core.Constants
import java.io.BufferedReader
import java.io.InputStreamReader

object LevelLoader {

    private const val TAG = "LevelLoader"

    fun loadLevel(context: Context, levelPath: String): Level? {
        return try {
            val inputStream = context.assets.open(levelPath)
            val reader = BufferedReader(InputStreamReader(inputStream))

            val lines = mutableListOf<String>()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                lines.add(line)
            }
            reader.close()

            parseLevel(lines)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load level: $levelPath", e)
            null
        }
    }

    private fun parseLevel(lines: List<String>): Level {
        // Parse header
        var width = Constants.WORLD_WIDTH
        var height = Constants.WORLD_HEIGHT
        var spawnX = 2f
        var spawnY = 2f
        var goalX = width - 2f
        var goalY = 2f

        val hazards = mutableListOf<Level.Hazard>()
        val tileLines = mutableListOf<String>()

        var parsingTiles = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

            if (trimmed == "TILES") {
                parsingTiles = true
                continue
            }

            if (!parsingTiles) {
                // Parse key=value pairs
                val parts = trimmed.split("=")
                if (parts.size == 2) {
                    val key = parts[0].trim().lowercase()
                    val value = parts[1].trim()
                    when (key) {
                        "width" -> width = value.toFloat()
                        "height" -> height = value.toFloat()
                        "spawnx" -> spawnX = value.toFloat()
                        "spawny" -> spawnY = value.toFloat()
                        "goalx" -> goalX = value.toFloat()
                        "goaly" -> goalY = value.toFloat()
                        "hazard" -> {
                            // Format: hazard=x,y,width,height
                            val hazardParts = value.split(",")
                            if (hazardParts.size == 4) {
                                hazards.add(Level.Hazard(
                                    hazardParts[0].toFloat(),
                                    hazardParts[1].toFloat(),
                                    hazardParts[2].toFloat(),
                                    hazardParts[3].toFloat()
                                ))
                            }
                        }
                    }
                }
            } else {
                tileLines.add(trimmed)
            }
        }

        // Parse tiles (bottom-up, so reverse)
        val heightTiles = height.toInt()
        val widthTiles = width.toInt()
        val tiles = Array(heightTiles) { IntArray(widthTiles) { 0 } }

        for (row in tileLines.withIndex()) {
            val y = heightTiles - 1 - row.index
            if (y < 0 || y >= heightTiles) continue

            val chars = row.value.toCharArray()
            for (x in 0 until minOf(widthTiles, chars.size)) {
                tiles[y][x] = when (chars[x]) {
                    '#', '1', 'X' -> 1
                    else -> 0
                }
            }
        }

        return Level(
            width = width,
            height = height,
            widthTiles = widthTiles,
            heightTiles = heightTiles,
            tiles = tiles,
            spawnX = spawnX,
            spawnY = spawnY,
            goalX = goalX,
            goalY = goalY,
            hazards = hazards
        )
    }

    fun getLevelList(context: Context, world: String): List<String> {
        val levels = mutableListOf<String>()
        try {
            val files = context.assets.list("levels/$world")
            files?.forEach { file ->
                if (file.endsWith(".txt")) {
                    levels.add("levels/$world/$file")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list levels for world: $world", e)
        }
        return levels.sorted()
    }
}