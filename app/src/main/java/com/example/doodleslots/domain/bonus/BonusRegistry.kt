package com.example.doodleslots.domain.bonus

/**
 * Реестр бонусов. Здесь регистрируются все эффекты.
 * Чтобы добавить новый — создай BonusEffect и добавь его сюда.
 */
object BonusRegistry {

    private val effects: Map<Int, BonusEffect> = listOf(
        SuperJumpBonus(),
        EnemyClearBonus(),
        CrystalBonus()
    ).associateBy { it.id }

    fun get(id: Int?): BonusEffect? = if (id == null) null else effects[id]
}