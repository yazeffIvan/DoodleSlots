package com.example.doodleslots.domain.daily

import android.util.Log
import com.example.doodleslots.domain.repository.DailyQuestRepository

class DailyQuestManager(private val repo: DailyQuestRepository) {

    private fun today(): Long = System.currentTimeMillis() / 86_400_000L

    fun ensureToday(): Boolean {
        val today = today()
        val lastLogin = repo.getLastLoginDay()
        val savedDay = repo.getSavedDay()

        var dayChanged = false

        // --- Стрик ---
        if (lastLogin == -1L) {
            repo.saveStreak(1)
        } else if (today > lastLogin) {
            if (today - lastLogin == 1L) {
                repo.saveStreak(repo.getStreak() + 1)
            } else {
                repo.saveStreak(1)
            }
        }
        repo.saveLastLoginDay(today)

        // --- Задания ---
        val currentQuests = loadQuests()
        if (savedDay != today || currentQuests.isEmpty()) {
            val newQuests = DailyQuestCatalog.pickDaily(today)
            repo.saveDay(today)
            val raw = serialize(newQuests)
            repo.saveQuestsRaw(raw)
            Log.d("DailyQuest", "generated: $raw")
            dayChanged = true
        }

        return dayChanged
    }

    fun loadQuests(): List<DailyQuest> {
        val raw = repo.getQuestsRaw()
        if (raw.isBlank()) return emptyList()
        return deserialize(raw)
    }

    fun getStreak(): Int = repo.getStreak()

    fun addProgress(type: QuestType, delta: Int, absolute: Boolean = false) {
        val quests = loadQuests().toMutableList()
        if (quests.isEmpty()) return
        var changed = false

        for (i in quests.indices) {
            val q = quests[i]
            if (q.type != type) continue
            if (q.completed || q.claimed) continue

            val newProgress = if (absolute) maxOf(q.progress, delta) else q.progress + delta
            val clamped = newProgress.coerceAtMost(q.target)
            val completed = clamped >= q.target

            quests[i] = q.copy(progress = clamped, completed = completed)
            changed = true
        }

        if (changed) repo.saveQuestsRaw(serialize(quests))
    }

    fun claim(id: String): Int {
        val quests = loadQuests().toMutableList()
        val idx = quests.indexOfFirst { it.id == id }
        if (idx < 0) return 0

        val q = quests[idx]
        if (!q.isReadyToClaim()) return 0

        quests[idx] = q.copy(claimed = true, completed = true)
        repo.saveQuestsRaw(serialize(quests))
        return q.reward
    }

    fun checkStreakBonus(): Int {
        val streak = getStreak()
        return when {
            streak > 0 && streak % 7 == 0 -> 1000
            streak > 0 && streak % 3 == 0 -> 200
            else -> 0
        }
    }

    private fun serialize(quests: List<DailyQuest>): String =
        quests.joinToString(";") { "${it.id}:${it.progress}:${if (it.claimed) 1 else 0}" }

    private fun deserialize(raw: String): List<DailyQuest> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size != 3) return@mapNotNull null
            val id = parts[0]
            val progress = parts[1].toIntOrNull() ?: 0
            val claimed = parts[2] == "1"
            val base = DailyQuestCatalog.byId(id) ?: return@mapNotNull null
            base.copy(
                progress = progress,
                completed = progress >= base.target,
                claimed = claimed
            )
        }
    }
}