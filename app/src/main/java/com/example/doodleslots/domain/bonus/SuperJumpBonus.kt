package com.example.doodleslots.domain.bonus

import com.example.doodleslots.domain.engine.GameEngine
import com.example.doodleslots.domain.model.PlatformModel

/**
 * Бонус 1: супер-прыжок. Подбрасывает игрока выше обычного.
 */
class SuperJumpBonus : BonusEffect {
    override val id: Int = 1

    override fun apply(engine: GameEngine, platform: PlatformModel) {
        engine.velocityY = engine.params.jumpNormal * 1.5f
    }
}