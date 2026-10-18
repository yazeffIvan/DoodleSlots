package com.example.doodleslots.domain.model

data class PlatformModel(
    var x: Float,
    var y: Float,
    val width: Float = 160f,
    val height: Float = 35f,
    var bonusType: Int? = null,
    /** 0 — обычная, 1 — исчезающая, 2 — двигающаяся, 3 — хрупкая, 4 — золотая */
    var platformType: Int = 0,
    /** Таймер жизни для исчезающих/хрупких (сек) */
    var lifeTimer: Float = -1f,
    /** Скорость движения (для двигающихся) */
    var moveSpeed: Float = 0f
)

data class EnemyModel(
    var x: Float,
    var y: Float,
    val width: Float = 70f,
    val height: Float = 70f,
    var speed: Float,
    val type: Int = 1,
    var hp: Int = 1
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    val color: Int,
    val size: Float
)

data class TrailPoint(
    val x: Float,
    val y: Float,
    var alpha: Float
)

data class Bullet(
    var x: Float,
    var y: Float,
    val vy: Float = -14f
)

data class Coin(
    var x: Float,
    var y: Float,
    var vy: Float = -3f
)