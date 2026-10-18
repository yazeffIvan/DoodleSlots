package com.example.doodleslots.presentation.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.TextureView
import com.example.doodleslots.R
import com.example.doodleslots.domain.engine.GameConfig
import com.example.doodleslots.domain.engine.GameEngine
import kotlin.concurrent.thread
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random as KRandom

class GameView(context: Context, val engine: GameEngine) : TextureView(context), TextureView.SurfaceTextureListener {

    private var renderThread: Thread? = null
    @Volatile private var running = false

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val bgTop = AndroidColor.rgb(10, 14, 26)
    private val bgBottom = AndroidColor.rgb(26, 31, 58)
    private val gridColor = AndroidColor.argb(30, 0, 229, 255)
    private val neonCyan = AndroidColor.rgb(0, 229, 255)
    private val neonGreen = AndroidColor.rgb(0, 230, 118)
    private val neonRed = AndroidColor.rgb(255, 23, 68)
    private val neonPurple = AndroidColor.rgb(124, 77, 255)
    private val neonOrange = AndroidColor.rgb(255, 140, 0)
    private val gold = AndroidColor.rgb(255, 215, 0)
    private val playerColor = AndroidColor.rgb(120, 220, 255)
    private val bulletColor = AndroidColor.rgb(255, 230, 100)

    private val bonus1: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.depyvremya)
    private val bonus2: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.zapretdep)
    private val bonus3: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.comingtododep)

    private val bonusRect = RectF()
    private val srcRect = Rect()
    private val bonusPath = Path()

    var onHudUpdate: (() -> Unit)? = null

    // Погода
    private enum class Weather { CLEAR, CLOUDS, RAIN, SNOW, STORM }
    private var currentWeather = Weather.CLEAR
    private var nextWeather = Weather.CLEAR
    private var weatherTransition = 1f
    private var weatherTimer = 0f
    private var weatherDuration = 12f
    private val weatherTransitions = 3f

    private val raindrops = MutableList(80) {
        Raindrop(
            x = KRandom.nextFloat() * GameConfig.WORLD_WIDTH,
            y = KRandom.nextFloat() * GameConfig.WORLD_HEIGHT,
            speed = 8f + KRandom.nextFloat() * 6f,
            length = 15f + KRandom.nextFloat() * 15f
        )
    }
    private val snowflakes = MutableList(60) {
        Snowflake(
            x = KRandom.nextFloat() * GameConfig.WORLD_WIDTH,
            y = KRandom.nextFloat() * GameConfig.WORLD_HEIGHT,
            speed = 1.5f + KRandom.nextFloat() * 2f,
            size = 3f + KRandom.nextFloat() * 3f,
            drift = KRandom.nextFloat() * 2f - 1f
        )
    }
    private val clouds = MutableList(6) {
        Cloud(
            x = KRandom.nextFloat() * GameConfig.WORLD_WIDTH,
            y = KRandom.nextFloat() * 400f,
            speed = 0.3f + KRandom.nextFloat() * 0.4f,
            size = 80f + KRandom.nextFloat() * 60f
        )
    }

    private var lightningActive = false
    private var lightningTimer = 0f
    private var lightningX = 0f
    private var lightningSeed = 0f

    init {
        surfaceTextureListener = this
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) { startRenderThread() }
    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {}
    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean { stopRenderThread(); return true }
    override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}

    private fun startRenderThread() {
        if (running) return
        running = true
        renderThread = thread(name = "GameLoop") {
            val frameNs = 16_666_667L
            var nextFrame = System.nanoTime()
            var lastHud = 0L
            while (running) {
                try { engine.updatePhysics() } catch (t: Throwable) { Log.e("GameView", "physics", t) }
                try { updateWeather(1f / 60f) } catch (t: Throwable) { Log.e("GameView", "weather", t) }
                try {
                    val canvas = lockCanvas(null)
                    if (canvas != null) {
                        synchronized(this) { drawGame(canvas) }
                        unlockCanvasAndPost(canvas)
                    }
                } catch (t: Throwable) { Log.e("GameView", "draw", t) }

                val now = System.currentTimeMillis()
                if (now - lastHud > 300) { lastHud = now; post { onHudUpdate?.invoke() } }

                nextFrame += frameNs
                val sleepNs = nextFrame - System.nanoTime()
                if (sleepNs > 0) {
                    try { Thread.sleep(sleepNs / 1_000_000, (sleepNs % 1_000_000).toInt()) }
                    catch (_: InterruptedException) {}
                } else nextFrame = System.nanoTime()
            }
        }
    }

    private fun stopRenderThread() { running = false; renderThread?.join(500); renderThread = null }

    private fun updateWeather(dt: Float) {
        weatherTimer += dt
        if (weatherTransition < 1f) {
            weatherTransition += dt / weatherTransitions
            if (weatherTransition > 1f) { weatherTransition = 1f; currentWeather = nextWeather }
        }
        if (weatherTimer >= weatherDuration && weatherTransition >= 1f) {
            weatherTimer = 0f
            weatherTransition = 0f
            currentWeather = nextWeather
            nextWeather = pickNextWeather(currentWeather)
            weatherDuration = 10f + KRandom.nextFloat() * 10f
        }

        val rainIntensity = weatherIntensity(Weather.RAIN) + weatherIntensity(Weather.STORM)
        if (rainIntensity > 0.01f) {
            for (r in raindrops) {
                r.y += r.speed
                if (r.y > GameConfig.WORLD_HEIGHT) { r.y = -20f; r.x = KRandom.nextFloat() * GameConfig.WORLD_WIDTH }
            }
        }
        val snowIntensity = weatherIntensity(Weather.SNOW)
        if (snowIntensity > 0.01f) {
            for (s in snowflakes) {
                s.y += s.speed
                s.x += sin(s.y * 0.05f) * s.drift
                if (s.y > GameConfig.WORLD_HEIGHT) { s.y = -20f; s.x = KRandom.nextFloat() * GameConfig.WORLD_WIDTH }
                if (s.x < 0f) s.x = GameConfig.WORLD_WIDTH
                if (s.x > GameConfig.WORLD_WIDTH) s.x = 0f
            }
        }
        val cloudIntensity = weatherIntensity(Weather.CLOUDS) + weatherIntensity(Weather.RAIN) + weatherIntensity(Weather.SNOW) + weatherIntensity(Weather.STORM)
        if (cloudIntensity > 0.01f) {
            for (c in clouds) {
                c.x += c.speed
                if (c.x > GameConfig.WORLD_WIDTH + 150f) { c.x = -150f; c.y = KRandom.nextFloat() * 400f }
            }
        }
        val stormIntensity = weatherIntensity(Weather.STORM)
        if (stormIntensity > 0.5f) {
            lightningTimer -= dt
            if (lightningTimer <= 0f) {
                lightningActive = true
                lightningX = KRandom.nextFloat() * GameConfig.WORLD_WIDTH
                lightningSeed = KRandom.nextFloat() * 100f
                lightningTimer = 0.15f
            }
            if (lightningActive) {
                lightningTimer -= dt
                if (lightningTimer <= -0.1f) lightningActive = false
            }
        } else {
            lightningActive = false
        }
    }

    private fun weatherIntensity(w: Weather): Float {
        if (currentWeather == w && weatherTransition >= 1f) return 1f
        if (currentWeather == w) return 1f - weatherTransition
        if (nextWeather == w) return weatherTransition
        return 0f
    }

    private fun pickNextWeather(cur: Weather): Weather = when (cur) {
        Weather.CLEAR -> if (KRandom.nextFloat() < 0.5f) Weather.CLOUDS else Weather.RAIN
        Weather.CLOUDS -> if (KRandom.nextFloat() < 0.5f) Weather.RAIN else Weather.CLEAR
        Weather.RAIN -> if (KRandom.nextFloat() < 0.4f) Weather.STORM else Weather.SNOW
        Weather.SNOW -> if (KRandom.nextFloat() < 0.6f) Weather.CLOUDS else Weather.STORM
        Weather.STORM -> if (KRandom.nextFloat() < 0.6f) Weather.RAIN else Weather.CLEAR
    }

    private fun drawGame(c: Canvas) {
        val w = c.width.toFloat()
        val h = c.height.toFloat()

        c.drawColor(AndroidColor.BLACK)
        paint.shader = null; paint.alpha = 255; paint.style = Paint.Style.FILL

        val stormTint = weatherIntensity(Weather.STORM) * 0.3f
        val topColor = lerpColor(bgTop, AndroidColor.rgb(5, 5, 15), stormTint)
        val bottomColor = lerpColor(bgBottom, AndroidColor.rgb(15, 15, 30), stormTint)

        paint.shader = LinearGradient(0f, 0f, 0f, h, topColor, bottomColor, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
        drawGrid(c, w, h)

        val scale = h / GameConfig.WORLD_HEIGHT
        val worldPixelWidth = GameConfig.WORLD_WIDTH * scale
        val offsetX = (w - worldPixelWidth) / 2f

        c.save(); c.translate(offsetX, 0f); c.scale(scale, scale)

        drawClouds(c)
        drawMoon(c)
        drawTrail(c)
        for (p in engine.platforms) drawPlatform(c, p.x, p.y, p.width, p.height, p.bonusType)
        for (e in engine.enemies) drawEnemy(c, e)
        for (b in engine.bullets) drawBullet(c, b.x, b.y)
        for (coin in engine.coins) drawCoin(c, coin.x, coin.y)
        drawPlayer(c)
        for (pt in engine.particles) drawParticle(c, pt.x, pt.y, pt.color, pt.alpha, pt.size)
        drawRain(c)
        drawSnow(c)
        drawLightning(c)

        c.restore()
    }

    private fun drawGrid(c: Canvas, w: Float, h: Float) {
        paint.shader = null; paint.color = gridColor; paint.strokeWidth = 1f
        val step = 40f
        var x = 0f; while (x < w) { c.drawLine(x, 0f, x, h, paint); x += step }
        var y = 0f; while (y < h) { c.drawLine(0f, y, w, y, paint); y += step }
    }

    private fun drawClouds(c: Canvas) {
        val intensity = weatherIntensity(Weather.CLOUDS) + weatherIntensity(Weather.RAIN) + weatherIntensity(Weather.SNOW) + weatherIntensity(Weather.STORM)
        if (intensity < 0.01f) return
        val baseAlpha = (intensity * 120).toInt().coerceIn(0, 200)
        paint.shader = null
        for (cloud in clouds) {
            paint.color = AndroidColor.argb(baseAlpha, 200, 210, 230)
            c.drawCircle(cloud.x, cloud.y, cloud.size, paint)
            c.drawCircle(cloud.x + cloud.size * 0.7f, cloud.y + cloud.size * 0.2f, cloud.size * 0.8f, paint)
            c.drawCircle(cloud.x - cloud.size * 0.7f, cloud.y + cloud.size * 0.2f, cloud.size * 0.8f, paint)
            c.drawCircle(cloud.x, cloud.y - cloud.size * 0.3f, cloud.size * 0.75f, paint)
        }
    }

    private fun drawRain(c: Canvas) {
        val intensity = weatherIntensity(Weather.RAIN) + weatherIntensity(Weather.STORM)
        if (intensity < 0.01f) return
        val alpha = (intensity * 200).toInt().coerceIn(0, 220)
        paint.shader = null; paint.color = AndroidColor.argb(alpha, 150, 200, 255); paint.strokeWidth = 2f
        for (r in raindrops) c.drawLine(r.x, r.y, r.x - 1f, r.y + r.length, paint)
    }

    private fun drawSnow(c: Canvas) {
        val intensity = weatherIntensity(Weather.SNOW)
        if (intensity < 0.01f) return
        val alpha = (intensity * 230).toInt().coerceIn(0, 240)
        paint.shader = null; paint.color = AndroidColor.argb(alpha, 255, 255, 255)
        for (s in snowflakes) c.drawCircle(s.x, s.y, s.size, paint)
    }

    private fun drawLightning(c: Canvas) {
        if (!lightningActive) return
        if (weatherIntensity(Weather.STORM) < 0.5f) return
        paint.shader = null
        paint.color = AndroidColor.argb(80, 220, 230, 255)
        c.drawRect(-200f, -200f, GameConfig.WORLD_WIDTH + 200f, GameConfig.WORLD_HEIGHT + 200f, paint)
        paint.color = AndroidColor.argb(230, 240, 245, 255); paint.strokeWidth = 4f
        var x = lightningX; var y = 0f
        val stepY = 40f
        var seed = lightningSeed
        while (y < GameConfig.WORLD_HEIGHT * 0.8f) {
            seed += 1.7f
            val nx = x + sin(seed) * 30f
            val ny = y + stepY
            c.drawLine(x, y, nx, ny, paint)
            x = nx; y = ny
        }
    }

    private fun drawMoon(c: Canvas) {
        val cx = engine.moonX + 50f; val cy = engine.moonY + 50f
        val cloudsDark = weatherIntensity(Weather.CLOUDS) + weatherIntensity(Weather.RAIN) + weatherIntensity(Weather.SNOW) + weatherIntensity(Weather.STORM)
        val moonAlpha = (255 * (1f - cloudsDark * 0.7f)).toInt().coerceIn(0, 255)
        glowPaint.shader = RadialGradient(cx, cy, 120f,
            intArrayOf(AndroidColor.argb((80 * (moonAlpha / 255f)).toInt(), 255, 215, 0), AndroidColor.argb(0, 255, 215, 0)),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, 120f, glowPaint); glowPaint.shader = null
        paint.shader = null
        paint.color = AndroidColor.argb(moonAlpha, 255, 215, 0); c.drawCircle(cx, cy, 50f, paint)
        paint.color = AndroidColor.argb((moonAlpha * 0.5f).toInt(), 255, 255, 200); c.drawCircle(cx - 15f, cy - 15f, 20f, paint)
    }

    private fun drawTrail(c: Canvas) {
        paint.shader = null
        val trailColor = when (engine.trailColorId) {
            "purple" -> neonPurple
            "red" -> neonRed
            "green" -> neonGreen
            else -> gold
        }
        for (tp in engine.trailPoints) {
            val a = (tp.alpha * 200).toInt().coerceIn(0, 255)
            paint.color = (trailColor and 0x00FFFFFF) or (a shl 24)
            c.drawCircle(tp.x, tp.y, 35f * tp.alpha, paint)
        }
    }

    private fun drawPlatform(c: Canvas, x: Float, y: Float, w: Float, h: Float, bonus: Int?) {
        // Ищем платформу для доступа к типу и таймеру
        val platform = engine.platforms.firstOrNull { it.x == x && it.y == y }

        paint.shader = null
        val accent = when (platform?.platformType) {
            1 -> AndroidColor.rgb(150, 150, 150)   // исчезающая — серая
            2 -> AndroidColor.rgb(30, 144, 255)    // двигающаяся — синяя
            3 -> AndroidColor.rgb(200, 50, 50)     // хрупкая — красная
            4 -> gold                              // золотая
            else -> when (bonus) { 1 -> neonCyan; 2 -> neonRed; 3 -> gold; else -> neonPurple }
        }

        // Прозрачность исчезающих — чем меньше таймер, тем прозрачнее
        val alpha = if (platform?.platformType == 1 && platform.lifeTimer > 0f) {
            (255 * (platform.lifeTimer / GameConfig.HARDCORE_VANISH_TIMER)).toInt().coerceIn(50, 255)
        } else 255

        glowPaint.shader = RadialGradient(x + w / 2f, y + h / 2f, w / 1.5f,
            intArrayOf((accent and 0x00FFFFFF) or ((80 * alpha / 255) shl 24), (accent and 0x00FFFFFF) or (0 shl 24)),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(x - 10f, y - 10f, x + w + 10f, y + h + 10f, glowPaint)
        glowPaint.shader = null

        paint.color = (accent and 0x00FFFFFF) or (alpha shl 24)
        c.drawRoundRect(x, y, x + w, y + h, 10f, 10f, paint)

        paint.color = AndroidColor.argb((180 * alpha / 255), 255, 255, 255)
        c.drawRoundRect(x + 4f, y + 2f, x + w - 4f, y + 6f, 4f, 4f, paint)

        // Индикатор "хрупкая" — пунктир
        if (platform?.platformType == 3) {
            paint.color = AndroidColor.argb(200, 255, 200, 200)
            paint.strokeWidth = 2f
            var dx = x + 8f
            while (dx < x + w - 8f) {
                c.drawLine(dx, y + h - 6f, dx + 6f, y + h - 6f, paint)
                dx += 12f
            }
        }

        if (bonus != null) {
            val bmp = when (bonus) { 1 -> bonus1; 2 -> bonus2; 3 -> bonus3; else -> null }
            drawCircularBonus(c, x + w / 2f, y - 45f, 32f, bmp)
        }
    }

    private fun drawCircularBonus(c: Canvas, cx: Float, cy: Float, radius: Float, bmp: Bitmap?) {
        glowPaint.shader = RadialGradient(cx, cy, radius * 2f,
            intArrayOf((neonCyan and 0x00FFFFFF) or (100 shl 24), (neonCyan and 0x00FFFFFF) or (0 shl 24)),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, radius * 2f, glowPaint); glowPaint.shader = null
        if (bmp != null) {
            bonusPath.reset(); bonusPath.addCircle(cx, cy, radius, Path.Direction.CW)
            c.save(); c.clipPath(bonusPath)
            srcRect.set(0, 0, bmp.width, bmp.height)
            bonusRect.set(cx - radius, cy - radius, cx + radius, cy + radius)
            paint.shader = null; paint.alpha = 255
            c.drawBitmap(bmp, srcRect, bonusRect, paint)
            c.restore()
        } else { paint.shader = null; paint.color = neonCyan; c.drawCircle(cx, cy, radius, paint) }
        paint.shader = null; paint.color = AndroidColor.WHITE; paint.style = Paint.Style.STROKE; paint.strokeWidth = 3f
        c.drawCircle(cx, cy, radius, paint); paint.style = Paint.Style.FILL
    }

    private fun drawEnemy(c: Canvas, e: com.example.doodleslots.domain.model.EnemyModel) {
        paint.shader = null
        val cx = e.x + e.width / 2f
        val cy = e.y + e.height / 2f
        val r = e.width / 2f

        val color = when (e.type) { 1 -> neonRed; 2 -> neonOrange; else -> neonPurple }

        glowPaint.shader = RadialGradient(cx, cy, r * 1.8f,
            intArrayOf((color and 0x00FFFFFF) or (120 shl 24), (color and 0x00FFFFFF) or (0 shl 24)),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, r * 1.8f, glowPaint); glowPaint.shader = null

        when (e.type) {
            1 -> {
                val path = Path()
                for (i in 0 until 6) {
                    val angle = (Math.PI / 3 * i - Math.PI / 2).toFloat()
                    val px = cx + r * cos(angle); val py = cy + r * sin(angle)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close(); paint.color = color; c.drawPath(path, paint)
            }
            2 -> {
                val path = Path().apply {
                    moveTo(cx, cy - r); lineTo(cx + r, cy + r * 0.8f); lineTo(cx - r, cy + r * 0.8f); close()
                }
                paint.color = color; c.drawPath(path, paint)
            }
            else -> {
                val path = Path().apply {
                    moveTo(cx, cy - r); lineTo(cx + r, cy); lineTo(cx, cy + r); lineTo(cx - r, cy); close()
                }
                paint.color = color; c.drawPath(path, paint)
            }
        }

        paint.color = AndroidColor.argb(200, 255, 255, 255)
        c.drawCircle(cx, cy, r * 0.35f, paint)
        paint.color = AndroidColor.BLACK
        c.drawCircle(cx, cy, r * 0.15f, paint)

        if (e.hp > 1) {
            val barW = e.width
            val barH = 6f
            val barX = e.x
            val barY = e.y - 15f
            paint.color = AndroidColor.argb(180, 0, 0, 0)
            c.drawRoundRect(barX, barY, barX + barW, barY + barH, 3f, 3f, paint)
            paint.color = neonGreen
            c.drawRoundRect(barX, barY, barX + barW * (e.hp / 2f), barY + barH, 3f, 3f, paint)
        }
    }

    private fun drawBullet(c: Canvas, x: Float, y: Float) {
        paint.shader = null
        glowPaint.shader = RadialGradient(x, y, 30f,
            intArrayOf((bulletColor and 0x00FFFFFF) or (150 shl 24), (bulletColor and 0x00FFFFFF) or (0 shl 24)),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        c.drawCircle(x, y, 30f, glowPaint); glowPaint.shader = null
        paint.color = bulletColor; c.drawCircle(x, y, GameConfig.BULLET_RADIUS, paint)
        paint.color = AndroidColor.WHITE; c.drawCircle(x, y, GameConfig.BULLET_RADIUS * 0.5f, paint)
    }

    private fun drawCoin(c: Canvas, x: Float, y: Float) {
        paint.shader = null
        glowPaint.shader = RadialGradient(x, y, 25f,
            intArrayOf((gold and 0x00FFFFFF) or (140 shl 24), (gold and 0x00FFFFFF) or (0 shl 24)),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        c.drawCircle(x, y, 25f, glowPaint); glowPaint.shader = null
        paint.color = gold
        c.drawCircle(x, y, 12f, paint)
        paint.color = AndroidColor.rgb(180, 140, 0)
        c.drawCircle(x, y, 8f, paint)
        paint.color = gold
        paint.textSize = 14f
        paint.textAlign = Paint.Align.CENTER
        c.drawText("$", x, y + 5f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawPlayer(c: Canvas) {
        paint.shader = null
        val cx = engine.playerX + 50f
        val cy = engine.playerY + 50f
        val stretch = (engine.velocityY * 0.6f).coerceIn(-12f, 12f)
        val rx = 50f - stretch / 2f
        val ry = 50f + stretch / 2f

        val rect = RectF(cx - rx, cy - ry, cx + rx, cy + ry)
        paint.color = playerColor
        c.drawOval(rect, paint)

        paint.color = AndroidColor.argb(200, 255, 255, 255)
        c.drawCircle(cx - rx * 0.35f, cy - ry * 0.35f, rx * 0.25f, paint)

        paint.color = AndroidColor.rgb(30, 90, 140)
        c.drawCircle(cx, cy, rx * 0.35f, paint)

        // Индикатор щита
        if (engine.immuneTimer > 0f) {
            // Кольцо-щит
            paint.shader = null
            paint.color = neonCyan
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            c.drawCircle(cx, cy, rx + 12f, paint)
            paint.style = Paint.Style.FILL

            // Свечение вокруг кольца
            glowPaint.shader = RadialGradient(
                cx, cy, rx + 30f,
                intArrayOf(
                    (neonCyan and 0x00FFFFFF) or (80 shl 24),
                    (neonCyan and 0x00FFFFFF) or (0 shl 24)
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            c.drawCircle(cx, cy, rx + 30f, glowPaint)
            glowPaint.shader = null

            // Значок щита — рисуем фигурой (не эмодзи, чтобы работало везде)
            val shieldX = cx
            val shieldY = cy - ry - 25f
            val shieldPath = Path().apply {
                moveTo(shieldX, shieldY - 15f)
                lineTo(shieldX + 15f, shieldY - 5f)
                lineTo(shieldX + 15f, shieldY + 5f)
                lineTo(shieldX, shieldY + 15f)
                lineTo(shieldX - 15f, shieldY + 5f)
                lineTo(shieldX - 15f, shieldY - 5f)
                close()
            }
            paint.color = neonCyan
            c.drawPath(shieldPath, paint)
            paint.color = AndroidColor.WHITE
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            c.drawPath(shieldPath, paint)
            paint.style = Paint.Style.FILL
        }
    }

    private fun drawParticle(c: Canvas, x: Float, y: Float, color: Int, alpha: Float, size: Float) {
        paint.shader = null
        val a = (alpha * 255).toInt().coerceIn(0, 255)
        paint.color = (color and 0x00FFFFFF) or (a shl 24)
        c.drawCircle(x, y, size, paint)
    }

    private fun lerpColor(a: Int, b: Int, t: Float): Int {
        val tt = t.coerceIn(0f, 1f)
        val ar = (a shr 16) and 0xFF; val ag = (a shr 8) and 0xFF; val ab = a and 0xFF
        val br = (b shr 16) and 0xFF; val bg = (b shr 8) and 0xFF; val bb = b and 0xFF
        return AndroidColor.rgb(
            (ar + (br - ar) * tt).toInt(),
            (ag + (bg - ag) * tt).toInt(),
            (ab + (bb - ab) * tt).toInt()
        )
    }

    private data class Raindrop(var x: Float, var y: Float, val speed: Float, val length: Float)
    private data class Snowflake(var x: Float, var y: Float, val speed: Float, val size: Float, val drift: Float)
    private data class Cloud(var x: Float, var y: Float, val speed: Float, val size: Float)
}