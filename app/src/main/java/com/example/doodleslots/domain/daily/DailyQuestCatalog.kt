package com.example.doodleslots.domain.daily

import kotlin.random.Random

object DailyQuestCatalog {

    /** Пул возможных заданий. Каждое задание — с фиксированной целью и наградой */
    private val pool: List<DailyQuest> = listOf(
        DailyQuest("kill_20", QuestType.KILL_ENEMIES, 20, 100, "Убей 20 врагов", "💀"),
        DailyQuest("kill_50", QuestType.KILL_ENEMIES, 50, 300, "Убей 50 врагов", "☠️"),
        DailyQuest("kill_100", QuestType.KILL_ENEMIES, 100, 600, "Убей 100 врагов", "🔥"),
        DailyQuest("wave_3", QuestType.REACH_WAVE, 3, 200, "Дойди до волны 3", "🌊"),
        DailyQuest("wave_5", QuestType.REACH_WAVE, 5, 400, "Дойди до волны 5", "🌪"),
        DailyQuest("score_1000", QuestType.SCORE_POINTS, 1000, 250, "Набери 1000 очков", "📈"),
        DailyQuest("score_3000", QuestType.SCORE_POINTS, 3000, 500, "Набери 3000 очков", "🚀"),
        DailyQuest("money_500", QuestType.COLLECT_MONEY, 500, 150, "Собери 500 монет", "💰"),
        DailyQuest("spin_5", QuestType.SPIN_SLOTS, 5, 100, "Крути слоты 5 раз", "🎰"),
        DailyQuest("spin_15", QuestType.SPIN_SLOTS, 15, 250, "Крути слоты 15 раз", "🎲"),
        DailyQuest("slots_win_3", QuestType.WIN_SLOTS, 3, 200, "Выиграй в слотах 3 раза", "✨"),
        DailyQuest("buy_1", QuestType.BUY_ITEM, 1, 150, "Купи товар в магазине", "🛒")
    )

    /** Выбирает 3 случайных задания на день. Используем seed, чтобы у всех было одинаково за день */
    fun pickDaily(seed: Long): List<DailyQuest> {
        val rnd = Random(seed)
        return pool.shuffled(rnd).take(3).map { it.copy() }
    }

    fun byId(id: String): DailyQuest? = pool.firstOrNull { it.id == id }
}