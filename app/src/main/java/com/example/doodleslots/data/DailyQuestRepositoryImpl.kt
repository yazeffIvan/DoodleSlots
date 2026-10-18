package com.example.doodleslots.data

import android.content.Context
import com.example.doodleslots.domain.repository.DailyQuestRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyQuestRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : DailyQuestRepository {

    private val prefs = context.getSharedPreferences("daily_quests", Context.MODE_PRIVATE)

    override fun getSavedDay(): Long = prefs.getLong("saved_day", -1L)
    override fun saveDay(day: Long) = prefs.edit().putLong("saved_day", day).apply()

    override fun getQuestsRaw(): String = prefs.getString("quests_raw", "") ?: ""
    override fun saveQuestsRaw(raw: String) = prefs.edit().putString("quests_raw", raw).apply()

    override fun getStreak(): Int = prefs.getInt("streak", 0)
    override fun saveStreak(value: Int) = prefs.edit().putInt("streak", value).apply()

    override fun getLastLoginDay(): Long = prefs.getLong("last_login", -1L)
    override fun saveLastLoginDay(day: Long) = prefs.edit().putLong("last_login", day).apply()
}