package com.example.doodleslots.domain.bonus

import com.example.doodleslots.domain.engine.GameConfig
import com.example.doodleslots.domain.engine.GameEngine
import com.example.doodleslots.domain.model.PlatformModel

/**
 * Бонус 3: кристаллы. Даёт очки и деньги.
 */
class CrystalBonus : BonusEffect {
    override val id: Int = 3

    override fun apply(engine: GameEngine, platform: PlatformModel) {
        engine.score += GameConfig.BONUS_3_SCORE
        engine.money += GameConfig.BONUS_3_REWARD
        engine.velocityY = engine.params.jumpNormal
    }
}