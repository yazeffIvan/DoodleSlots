package com.example.doodleslots.domain.engine

object GameConfig {
    const val WORLD_WIDTH = 960f
    const val WORLD_HEIGHT = 1400f

    const val PLAYER_START_X = 400f
    const val PLAYER_START_Y = 700f
    const val PLAYER_SIZE = 100f
    const val PLAYER_MAX_HP = 3
    const val PLAYER_HP_PER_UPGRADE = 1

    const val PLATFORM_MARGIN = 50f
    const val TELEPORT_OVERLAP = 10f

    const val GRAVITY = 0.4f
    const val JUMP_NORMAL = -22f
    const val DOUBLE_JUMP_POWER = -18f

    const val MOVE_SPEED_BASE = 8f

    const val PLATFORM_STEP_Y = 130f
    const val WORLD_FILL_AHEAD = 2500f

    const val MONEY_PER_PLATFORM = 5
    const val MONEY_PER_SCROLL_DIV = 20
    const val BONUS_3_REWARD = 100
    const val BONUS_3_SCORE = 300
    const val SCORE_PER_ENEMY = 50
    const val MONEY_PER_KILL = 25

    const val DEATH_DIVIDER = 2

    const val BULLET_SPEED = -14f
    const val BULLET_RADIUS = 8f
    const val MANUAL_SHOOT_BURST = 3

    const val SHOOT_COOLDOWN_BASE = 0.5f
    const val SHOOT_COOLDOWN_UPGRADED = 0.25f

    const val ENEMY_MIN_COUNT = 2
    const val ENEMY_MAX_COUNT = 8
    const val ENEMY_FORCE_SPAWN_INTERVAL = 3f
    const val ENEMY_IMMUNE_TIME = 1.5f

    const val WAVE_DURATION = 30f
    const val WAVE_SPEED_MULT = 1.15f

    const val COMBO_TIMEOUT = 2.5f

    const val SHIELD_DURATION = 5f
    const val MAGNET_RADIUS = 250f
    const val MAGNET_FORCE = 6f
    const val SLOWMO_FACTOR = 0.4f

    const val BOOST_X2_MULTIPLIER = 2

    // === HARDCORE ===
    /** Сколько секунд живёт исчезающая платформа после касания */
    const val HARDCORE_VANISH_TIMER = 2.0f
    /** Множитель денег за платформы в hardcore */
    const val HARDCORE_MONEY_MULT = 2
    /** Скорость двигающихся платформ */
    const val HARDCORE_MOVING_SPEED = 1.8f
    /** Шаг платформ в hardcore — плотнее, чтобы всегда можно было допрыгнуть */
    const val HARDCORE_PLATFORM_STEP_Y = 100f
}