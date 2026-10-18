package com.example.doodleslots.domain.repository

interface DailyQuestRepository {
    /** Unix-day — число дня, для которого сохранены текущие задания */
    fun getSavedDay(): Long
    fun saveDay(day: Long)

    /** Три задания на текущий день в формате: id:progress:claimed;id:progress:claimed;... */
    fun getQuestsRaw(): String
    fun saveQuestsRaw(raw: String)

    /** Стрик (дней подряд) */
    fun getStreak(): Int
    fun saveStreak(value: Int)

    /** Когда последний раз заходил (unix-day) */
    fun getLastLoginDay(): Long
    fun saveLastLoginDay(day: Long)
}