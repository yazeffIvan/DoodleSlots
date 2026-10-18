package com.example.doodleslots.domain.engine

import com.example.doodleslots.domain.model.EnemyModel
import com.example.doodleslots.domain.model.PlatformModel
import kotlin.math.abs
import kotlin.random.Random

object WorldGenerator {

    fun fillWorld(
        platforms: MutableList<PlatformModel>,
        enemies: MutableList<EnemyModel>,
        targetTopY: Float,
        stepY: Float,
        waveLevel: Int,
        mode: GameMode = GameMode.NORMAL
    ) {
        if (platforms.size > 80) return

        var topY = platforms.minOfOrNull { it.y } ?: 1000f

        val platformWidth = 160f
        val margin = GameConfig.PLATFORM_MARGIN
        val usableWidth = GameConfig.WORLD_WIDTH - platformWidth - margin * 2

        while (topY > targetTopY && platforms.size < 80) {
            val prevTopY = topY
            topY -= stepY

            val platformsBelow = platforms.filter { it.y in (prevTopY - 5f)..(prevTopY + 5f) }

            val x: Float = if (platformsBelow.isNotEmpty()) {
                val ref = platformsBelow.random()
                val maxShift = 250f
                val minX = (ref.x - maxShift).coerceAtLeast(margin)
                val maxX = (ref.x + maxShift).coerceAtMost(margin + usableWidth)
                minX + Random.nextFloat() * (maxX - minX)
            } else {
                margin + Random.nextFloat() * usableWidth
            }

            val platform = if (mode == GameMode.HARDCORE) {
                generateHardcorePlatform(x, topY, platforms)
            } else {
                val bonus = if (Random.nextFloat() < 0.4f) Random.nextInt(1, 4) else null
                PlatformModel(x, topY, bonusType = bonus)
            }
            platforms.add(platform)

            // Вторая платформа на том же уровне
            val secondChance = if (mode == GameMode.HARDCORE) 0.6f else 0.35f
            if (Random.nextFloat() < secondChance) {
                val x2 = margin + Random.nextFloat() * usableWidth
                if (abs(x2 - x) > 220f) {
                    val p2 = if (mode == GameMode.HARDCORE) {
                        if (Random.nextFloat() < 0.15f) {
                            PlatformModel(x2, topY, platformType = 4)  // золотая
                        } else {
                            PlatformModel(x2, topY, platformType = 0)  // обычная
                        }
                    } else {
                        PlatformModel(x2, topY)
                    }
                    platforms.add(p2)
                }
            }

            // Враги
            if (Random.nextFloat() < 0.15f && enemies.size < GameConfig.ENEMY_MAX_COUNT) {
                val enemyY = topY - 60f
                val nearRespawn = abs(enemyY - GameConfig.PLAYER_START_Y) < 250f
                if (!nearRespawn) {
                    spawnEnemy(enemies, margin, usableWidth, enemyY, waveLevel)
                }
            }
        }
    }

    /**
     * Генерирует платформу для HARDCORE.
     * ВАЖНО: если на предыдущем уровне была опасная платформа,
     * текущая — гарантированно безопасная (обычная/золотая).
     */
    private fun generateHardcorePlatform(
        x: Float,
        y: Float,
        existing: List<PlatformModel>
    ): PlatformModel {
        val prevDangerous = existing.any {
            abs(it.y - (y + GameConfig.HARDCORE_PLATFORM_STEP_Y)) < 30f &&
                    (it.platformType == 1 || it.platformType == 3)
        }

        // Если внизу была опасная — сейчас безопасная
        if (prevDangerous) {
            return if (Random.nextFloat() < 0.15f) {
                PlatformModel(x, y, platformType = 4)   // золотая
            } else {
                PlatformModel(x, y, platformType = 0)   // обычная
            }
        }

        val roll = Random.nextFloat()
        return when {
            roll < 0.10f -> PlatformModel(x, y, platformType = 4)          // золотая
            roll < 0.25f -> PlatformModel(x, y, platformType = 3)          // хрупкая
            roll < 0.50f -> PlatformModel(
                x, y, platformType = 2,                                    // двигающаяся
                moveSpeed = if (Random.nextBoolean())
                    GameConfig.HARDCORE_MOVING_SPEED else -GameConfig.HARDCORE_MOVING_SPEED
            )
            roll < 0.70f -> PlatformModel(
                x, y, platformType = 1,
                lifeTimer = -1f                                            // исчезающая, таймер не активен
            )
            else -> PlatformModel(x, y, platformType = 0)                  // обычная
        }
    }

    fun forceSpawnEnemy(
        enemies: MutableList<EnemyModel>,
        topY: Float,
        waveLevel: Int
    ) {
        if (enemies.size >= GameConfig.ENEMY_MAX_COUNT) return
        val margin = GameConfig.PLATFORM_MARGIN
        val usableWidth = GameConfig.WORLD_WIDTH - 160f - margin * 2
        spawnEnemy(enemies, margin, usableWidth, topY, waveLevel)
    }

    private fun spawnEnemy(
        enemies: MutableList<EnemyModel>,
        margin: Float,
        usableWidth: Float,
        y: Float,
        waveLevel: Int
    ) {
        val type = when {
            waveLevel < 2 -> if (Random.nextBoolean()) 1 else 2
            else -> Random.nextInt(1, 4)
        }
        val baseSpeed = when (type) {
            1 -> 1.0f
            2 -> 2.2f
            else -> 1.6f
        }
        val speedMult = Math.pow(GameConfig.WAVE_SPEED_MULT.toDouble(), waveLevel.toDouble()).toFloat()
        val dir = if (Random.nextBoolean()) 1f else -1f

        enemies.add(
            EnemyModel(
                x = margin + Random.nextFloat() * (usableWidth - 70f),
                y = y,
                speed = baseSpeed * speedMult * dir,
                type = type,
                hp = if (type == 3) 2 else 1
            )
        )
    }
}