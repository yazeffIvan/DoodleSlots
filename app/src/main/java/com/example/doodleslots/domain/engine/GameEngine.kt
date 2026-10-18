package com.example.doodleslots.domain.engine

import com.example.doodleslots.domain.bonus.BonusRegistry
import com.example.doodleslots.domain.model.Bullet
import com.example.doodleslots.domain.model.Coin
import com.example.doodleslots.domain.model.EnemyModel
import com.example.doodleslots.domain.model.Particle
import com.example.doodleslots.domain.model.PlatformModel
import com.example.doodleslots.domain.model.TrailPoint
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
    val params: TunableParams,
    val ownedUpgrades: Set<String> = emptySet(),
    val activePerks: Set<String> = emptySet(),
    val activeBoosts: Set<String> = emptySet(),
    val mode: GameMode = GameMode.NORMAL
) {

    private val hasFirerateUpgrade = ownedUpgrades.contains("up_firerate")
    private val hasDamageUpgrade = ownedUpgrades.contains("up_damage")
    private val hasMagnetUpgrade = ownedUpgrades.contains("up_magnet")
    private val hasDoubleJumpPerk = activePerks.contains("perk_doublejump")
    private val hasShieldPerk = activePerks.contains("perk_shield")
    private val hasSlowmoPerk = activePerks.contains("perk_slowmo")
    private val hasLeverageBoost = activeBoosts.contains("boost_leverage")
    private val hasX2MoneyBoost = activeBoosts.contains("boost_x2money")
    private val hasSafeRespawnBoost = activeBoosts.contains("boost_saferespawn")

    private val hpBonus: Int = if (ownedUpgrades.contains("up_hp")) 1 else 0

    private val shootCooldown: Float =
        if (hasFirerateUpgrade) GameConfig.SHOOT_COOLDOWN_UPGRADED else GameConfig.SHOOT_COOLDOWN_BASE

    private val baseMoneyMult: Int =
        if (hasX2MoneyBoost) GameConfig.BOOST_X2_MULTIPLIER else 1

    private val moneyMultiplier: Int = baseMoneyMult *
            (if (mode == GameMode.HARDCORE) GameConfig.HARDCORE_MONEY_MULT else 1)

    /** Шаг платформ в текущем режиме */
    private val actualStepY: Float =
        if (mode == GameMode.HARDCORE) GameConfig.HARDCORE_PLATFORM_STEP_Y else params.platformStepY

    var playerX = GameConfig.PLAYER_START_X
    var playerY = GameConfig.PLAYER_START_Y
    var velocityX = 0f
    var velocityY = -10f

    var score = 0
    var money = 100
    var hp = GameConfig.PLAYER_MAX_HP + hpBonus
    var maxHp = GameConfig.PLAYER_MAX_HP + hpBonus
    var kills = 0

    var isDead = false

    var speedMultiplier = 1.0f
    var isLeverageActive = hasLeverageBoost

    var trailColorId = "green"

    var inputLeft = false
    var inputRight = false

    var moonX = 200f
    var moonY = 100f

    var waveLevel = 0
    private var waveTimer = 0f

    var comboMultiplier = 1
    private var comboTimer = 0f

    private var shootTimer = 0f
    private var forceSpawnTimer = 0f

    var immuneTimer = if (hasShieldPerk) GameConfig.SHIELD_DURATION else 0f

    private var doubleJumpAvailable = false
    private var wasOnPlatform = false

    var slowmoActive = false

    private var safeRespawnAvailable = hasSafeRespawnBoost

    val platforms = mutableListOf<PlatformModel>()
    val enemies = mutableListOf<EnemyModel>()
    val particles = mutableListOf<Particle>()
    val trailPoints = mutableListOf<TrailPoint>()
    val bullets = mutableListOf<Bullet>()
    val coins = mutableListOf<Coin>()

    init {
        platforms.add(PlatformModel(350f, 1000f))
        WorldGenerator.fillWorld(
            platforms, enemies,
            -GameConfig.WORLD_FILL_AHEAD,
            actualStepY,
            waveLevel,
            mode
        )
    }

    fun shoot() {
        if (isDead) return
        if (shootTimer > 0f) return
        shootTimer = shootCooldown
        repeat(GameConfig.MANUAL_SHOOT_BURST) { i ->
            bullets.add(Bullet(playerX + 50f + (i - 1) * 15f, playerY))
        }
    }

    fun doubleJump() {
        if (isDead) return
        if (!hasDoubleJumpPerk) return
        if (!doubleJumpAvailable) return
        velocityY = GameConfig.DOUBLE_JUMP_POWER
        doubleJumpAvailable = false
        spawnBurstParticles(playerX + 50f, playerY + 50f, 0xFFFFD700.toInt(), 8)
    }

    private fun die() {
        isDead = true
        inputLeft = false
        inputRight = false
    }

    fun updatePhysics() {
        if (isDead) return

        val baseDt = 1f / 60f
        val dt = if (slowmoActive) baseDt * GameConfig.SLOWMO_FACTOR else baseDt

        slowmoActive = hasSlowmoPerk && hp == 1

        if (shootTimer > 0f) shootTimer -= baseDt
        if (immuneTimer > 0f) immuneTimer -= baseDt
        if (comboTimer > 0f) {
            comboTimer -= baseDt
            if (comboTimer <= 0f) comboMultiplier = 1
        }

        waveTimer += baseDt
        if (waveTimer >= GameConfig.WAVE_DURATION) {
            waveTimer = 0f
            waveLevel++
        }

        val spd = speedMultiplier

        // Горизонталь
        velocityX = when {
            inputLeft && !inputRight -> -params.moveSpeedBase * spd
            inputRight && !inputLeft -> params.moveSpeedBase * spd
            else -> 0f
        }
        playerX += velocityX * (dt / baseDt)

        val halfPlayer = GameConfig.PLAYER_SIZE / 2f
        if (playerX + halfPlayer < 0f) playerX = GameConfig.WORLD_WIDTH - halfPlayer
        if (playerX + halfPlayer > GameConfig.WORLD_WIDTH) playerX = -halfPlayer

        velocityY += params.gravity * (dt / baseDt)
        playerY += velocityY * (dt / baseDt)

        // Шлейф
        if (velocityY < 0) {
            trailPoints.add(TrailPoint(playerX + 50f, playerY + 50f, 0.9f))
            if (trailPoints.size > 15) trailPoints.removeAt(0)
        }
        for (i in trailPoints.indices) trailPoints[i].alpha -= 0.06f
        trailPoints.removeAll { it.alpha <= 0f }

        // --- Обновление платформ ---
        val platformsToRemove = mutableListOf<PlatformModel>()
        for (p in platforms) {
            // Двигающаяся
            if (p.platformType == 2 && p.moveSpeed != 0f) {
                p.x += p.moveSpeed
                if (p.x < GameConfig.PLATFORM_MARGIN) {
                    p.x = GameConfig.PLATFORM_MARGIN
                    p.moveSpeed = -p.moveSpeed
                }
                if (p.x + p.width > GameConfig.WORLD_WIDTH - GameConfig.PLATFORM_MARGIN) {
                    p.x = GameConfig.WORLD_WIDTH - GameConfig.PLATFORM_MARGIN - p.width
                    p.moveSpeed = -p.moveSpeed
                }
            }
            // Исчезающая: тикает таймер только если он запущен (при касании)
            if (p.platformType == 1 && p.lifeTimer >= 0f) {
                p.lifeTimer -= baseDt
                if (p.lifeTimer <= 0f) {
                    platformsToRemove.add(p)
                }
            }
            // Хрупкая: таймер тоже может быть запущен
            if (p.platformType == 3 && p.lifeTimer >= 0f) {
                p.lifeTimer -= baseDt
                if (p.lifeTimer <= 0f) {
                    platformsToRemove.add(p)
                }
            }
        }
        if (platformsToRemove.isNotEmpty()) {
            for (p in platformsToRemove) {
                spawnBurstParticles(p.x + p.width / 2f, p.y + p.height / 2f, 0xFF888888.toInt(), 8)
            }
            platforms.removeAll { it in platformsToRemove }
        }

        // Пули
        for (i in bullets.indices.reversed()) {
            val b = bullets[i]
            b.y += GameConfig.BULLET_SPEED * (dt / baseDt)
            if (b.y < -100f) bullets.removeAt(i)
        }

        // Попадания
        for (i in bullets.indices.reversed()) {
            val b = bullets[i]
            var consumed = false
            for (j in enemies.indices.reversed()) {
                val e = enemies[j]
                if (b.x > e.x && b.x < e.x + e.width &&
                    b.y > e.y && b.y < e.y + e.height
                ) {
                    e.hp -= if (hasDamageUpgrade) 2 else 1
                    spawnBurstParticles(b.x, b.y, 0xFFFFE066.toInt(), 4)
                    consumed = true
                    if (e.hp <= 0) {
                        enemies.removeAt(j)
                        money += GameConfig.MONEY_PER_KILL * moneyMultiplier
                        score += GameConfig.SCORE_PER_ENEMY * comboMultiplier
                        kills++
                        comboMultiplier++
                        comboTimer = GameConfig.COMBO_TIMEOUT
                        coins.add(Coin(e.x + e.width / 2f, e.y + e.height / 2f))
                        spawnBurstParticles(e.x + e.width / 2f, e.y + e.height / 2f, 0xFF00E5FF.toInt(), 10)
                    }
                    break
                }
            }
            if (consumed) bullets.removeAt(i)
        }

        // Монеты
        for (i in coins.indices.reversed()) {
            val c = coins[i]
            c.vy += 0.3f
            c.y += c.vy

            if (hasMagnetUpgrade) {
                val dx = (playerX + 50f) - c.x
                val dy = (playerY + 50f) - c.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < GameConfig.MAGNET_RADIUS && dist > 1f) {
                    c.x += dx / dist * GameConfig.MAGNET_FORCE
                    c.y += dy / dist * GameConfig.MAGNET_FORCE
                }
            }

            if (abs(c.x - (playerX + 50f)) < 60f && abs(c.y - (playerY + 50f)) < 60f) {
                money += 10 * moneyMultiplier
                coins.removeAt(i)
                continue
            }

            if (c.y > 1500f) coins.removeAt(i)
        }

        // Частицы
        for (i in particles.indices.reversed()) {
            val pt = particles[i]
            pt.x += pt.vx
            pt.y += pt.vy
            pt.vy += 0.2f
            pt.alpha -= 0.04f
            if (pt.alpha <= 0f) particles.removeAt(i)
        }

        // Приземление
        var landedThisFrame = false
        if (velocityY > 0) {
            for (p in platforms) {
                if (playerX + 90f > p.x && playerX + 10f < p.x + p.width &&
                    playerY + 100f >= p.y && playerY + 100f <= p.y + p.height + 20f
                ) {
                    playerY = p.y - 100f
                    velocityY = params.jumpNormal * spd

                    val baseReward = GameConfig.MONEY_PER_PLATFORM * moneyMultiplier
                    val reward = if (p.platformType == 4) baseReward * 3 else baseReward
                    money += reward

                    // Запуск таймера исчезновения ТОЛЬКО при касании
                    if (p.platformType == 1 && p.lifeTimer < 0f) {
                        p.lifeTimer = GameConfig.HARDCORE_VANISH_TIMER
                    }
                    // Хрупкая — исчезает быстро после прыжка
                    if (p.platformType == 3 && p.lifeTimer < 0f) {
                        p.lifeTimer = 0.3f
                    }

                    handleBonus(p)
                    spawnLandingParticles()
                    landedThisFrame = true
                    break
                }
            }
        }

        if (landedThisFrame) {
            doubleJumpAvailable = hasDoubleJumpPerk
            wasOnPlatform = true
        } else if (velocityY > 0) {
            wasOnPlatform = false
        }
        if (!wasOnPlatform && velocityY < 0 && hasDoubleJumpPerk) {
            doubleJumpAvailable = true
        }

        // Враги
        for (e in enemies) {
            e.x += e.speed
            if (e.x < 0f || e.x > GameConfig.WORLD_WIDTH - e.width) {
                e.speed = -e.speed
                e.x = e.x.coerceIn(0f, GameConfig.WORLD_WIDTH - e.width)
            }

            if (immuneTimer <= 0f &&
                playerX + 90f > e.x && playerX + 10f < e.x + e.width &&
                playerY + 90f > e.y && playerY + 10f < e.y + e.height
            ) {
                hp--
                immuneTimer = GameConfig.ENEMY_IMMUNE_TIME
                velocityY = -14f
                comboMultiplier = 1
                comboTimer = 0f
                spawnBurstParticles(playerX + 50f, playerY + 50f, 0xFFFF1744.toInt(), 12)

                val idx = enemies.indexOf(e)
                if (idx >= 0) enemies.removeAt(idx)
                kills++
                score += 25
                money += 10
                spawnBurstParticles(e.x + e.width / 2f, e.y + e.height / 2f, 0xFFFFE066.toInt(), 10)

                if (hp <= 0) {
                    die()
                    return
                }
                break
            }
        }

        // Принудительный спавн
        forceSpawnTimer -= baseDt
        if (forceSpawnTimer <= 0f) {
            forceSpawnTimer = GameConfig.ENEMY_FORCE_SPAWN_INTERVAL
            if (enemies.size < GameConfig.ENEMY_MIN_COUNT + waveLevel) {
                val spawnY = playerY - 600f - Random.nextFloat() * 400f
                WorldGenerator.forceSpawnEnemy(enemies, spawnY, waveLevel)
            }
        }

        // Скролл
        if (playerY < 400f) {
            val diff = 400f - playerY
            playerY = 400f
            score += diff.toInt()
            money += (diff / GameConfig.MONEY_PER_SCROLL_DIV).toInt() * moneyMultiplier

            for (i in platforms.indices) platforms[i].y += diff
            platforms.removeAll { it.y > 1400f || it.y < -2200f }

            for (i in enemies.indices) enemies[i].y += diff
            enemies.removeAll { it.y > 1400f || it.y < -2200f }

            for (i in bullets.indices) bullets[i].y += diff
            bullets.removeAll { it.y > 1400f || it.y < -2200f }

            for (i in coins.indices) coins[i].y += diff
            coins.removeAll { it.y > 1400f || it.y < -2200f }

            WorldGenerator.fillWorld(
                platforms, enemies,
                -GameConfig.WORLD_FILL_AHEAD,
                actualStepY,
                waveLevel,
                mode
            )
        }

        // Падение
        if (playerY > 1500f) {
            hp--
            if (hp <= 0) {
                die()
                return
            } else {
                playerY = GameConfig.PLAYER_START_Y
                playerX = GameConfig.PLAYER_START_X
                velocityX = 0f
                velocityY = -10f
                immuneTimer = GameConfig.ENEMY_IMMUNE_TIME
            }
        }
    }

    fun consumeSafeRespawn(): Boolean {
        if (!safeRespawnAvailable) return false
        safeRespawnAvailable = false
        return true
    }

    private fun handleBonus(p: PlatformModel) {
        val effect = BonusRegistry.get(p.bonusType) ?: return
        effect.apply(this, p)
        p.bonusType = null
    }

    private fun spawnLandingParticles() {
        repeat(6) {
            val a = Random.nextFloat() * Math.PI.toFloat()
            val s = Random.nextFloat() * 3f + 1f
            particles.add(
                Particle(
                    x = playerX + 50f,
                    y = playerY + 100f,
                    vx = cos(a) * s,
                    vy = -sin(a) * s,
                    alpha = 1f,
                    color = 0xFF00FFFF.toInt(),
                    size = 4f
                )
            )
        }
    }

    private fun spawnBurstParticles(x: Float, y: Float, color: Int, count: Int) {
        repeat(count) {
            val a = Random.nextFloat() * 2 * Math.PI.toFloat()
            val s = Random.nextFloat() * 4f + 2f
            particles.add(
                Particle(
                    x = x, y = y,
                    vx = cos(a) * s, vy = sin(a) * s,
                    alpha = 1f, color = color, size = 6f
                )
            )
        }
    }
}