package com.example.doodleslots.data

import android.content.Context
import com.example.doodleslots.domain.engine.GameConfig
import com.example.doodleslots.domain.repository.TunableParamsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TunableParamsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : TunableParamsRepository {

    private val prefs = context.getSharedPreferences("tunable_params", Context.MODE_PRIVATE)

    override fun loadMoveSpeedBase(): Float = prefs.getFloat("moveSpeedBase", GameConfig.MOVE_SPEED_BASE)
    override fun loadGravity(): Float = prefs.getFloat("gravity", GameConfig.GRAVITY)
    override fun loadJumpNormal(): Float = prefs.getFloat("jumpNormal", GameConfig.JUMP_NORMAL)
    override fun loadPlatformStepY(): Float = prefs.getFloat("platformStepY", GameConfig.PLATFORM_STEP_Y)

    override fun save(
        moveSpeedBase: Float,
        gravity: Float,
        jumpNormal: Float,
        platformStepY: Float
    ) {
        prefs.edit()
            .putFloat("moveSpeedBase", moveSpeedBase)
            .putFloat("gravity", gravity)
            .putFloat("jumpNormal", jumpNormal)
            .putFloat("platformStepY", platformStepY)
            .apply()
    }

    override fun clearAll() = prefs.edit().clear().apply()
}