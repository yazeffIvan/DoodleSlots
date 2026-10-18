package com.example.doodleslots.domain.bonus

import com.example.doodleslots.domain.engine.GameEngine
import com.example.doodleslots.domain.model.PlatformModel

/**
 * Бонус 2: очистка всех врагов с экрана.
 * ВРЕМЕННО ОТКЛЮЧЁН — чтобы посмотреть, как играется без него.
 */
class EnemyClearBonus : BonusEffect {
    override val id: Int = 2

    override fun apply(engine: GameEngine, platform: PlatformModel) {
        // Временно отключено:
        // engine.enemies.clear()
        // engine.velocityY = engine.params.jumpNormal

        // Всё равно даём обычный прыжок, чтобы платформа не «залипала»
        engine.velocityY = engine.params.jumpNormal
    }
}