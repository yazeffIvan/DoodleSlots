package com.example.doodleslots.domain.repository

interface TunableParamsRepository {
    fun loadMoveSpeedBase(): Float
    fun loadGravity(): Float
    fun loadJumpNormal(): Float
    fun loadPlatformStepY(): Float

    fun save(
        moveSpeedBase: Float,
        gravity: Float,
        jumpNormal: Float,
        platformStepY: Float
    )

    fun clearAll()
}