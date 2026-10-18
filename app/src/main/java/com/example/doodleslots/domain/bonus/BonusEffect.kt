package com.example.doodleslots.domain.bonus

import com.example.doodleslots.domain.engine.GameEngine
import com.example.doodleslots.domain.model.PlatformModel

/**
 * Стратегия эффекта бонуса.
 * Каждый бонус — отдельный класс. Новый бонус = новый файл, движок не трогаем.
 */
interface BonusEffect {
    /** Уникальный ID, соответствующий platform.bonusType */
    val id: Int

    /** Применить эффект к движку и платформе */
    fun apply(engine: GameEngine, platform: PlatformModel)
}