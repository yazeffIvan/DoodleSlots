package com.example.doodleslots.domain.engine

import com.example.doodleslots.domain.repository.TunableParamsRepository

/**
 * Изменяемые параметры игры. Чистый домен, никакого Android.
 * При старте подтягивает сохранённые значения, при save() — пишет в репозиторий.
 */
class TunableParams(private val repository: TunableParamsRepository) {

    var moveSpeedBase: Float = repository.loadMoveSpeedBase()
    var gravity: Float = repository.loadGravity()
    var jumpNormal: Float = repository.loadJumpNormal()
    var platformStepY: Float = repository.loadPlatformStepY()

    /** Вернуть дефолты из GameConfig (в памяти, без сохранения) */
    fun reset() {
        moveSpeedBase = GameConfig.MOVE_SPEED_BASE
        gravity = GameConfig.GRAVITY
        jumpNormal = GameConfig.JUMP_NORMAL
        platformStepY = GameConfig.PLATFORM_STEP_Y
    }

    /** Сохранить текущие значения через репозиторий */
    fun save() {
        repository.save(
            moveSpeedBase = moveSpeedBase,
            gravity = gravity,
            jumpNormal = jumpNormal,
            platformStepY = platformStepY
        )
    }
}