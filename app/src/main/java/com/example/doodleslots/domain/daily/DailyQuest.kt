package com.example.doodleslots.domain.daily

/** Тип события, которое отслеживает задание */
enum class QuestType {
    KILL_ENEMIES,     // убить врагов
    REACH_WAVE,       // дойти до волны
    SCORE_POINTS,     // набрать очки
    COLLECT_MONEY,    // собрать денег в игре
    SPIN_SLOTS,       // крутить слоты
    WIN_SLOTS,        // выиграть в слотах
    BUY_ITEM          // купить товар
}

/** Одно задание */
data class DailyQuest(
    val id: String,
    val type: QuestType,
    val target: Int,
    val reward: Int,
    val title: String,
    val emoji: String,
    val progress: Int = 0,
    val completed: Boolean = false,
    val claimed: Boolean = false
) {
    val progressRatio: Float
        get() = if (target <= 0) 0f else (progress.toFloat() / target).coerceIn(0f, 1f)

    fun isReadyToClaim(): Boolean = progress >= target && !claimed
}