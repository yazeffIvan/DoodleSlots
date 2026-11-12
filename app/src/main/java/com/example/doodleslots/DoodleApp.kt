package com.example.doodleslots

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DoodleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // ВРЕМЕННЫЙ СБРОС ДЛЯ ТЕСТОВ
        // Очищаем SharedPreferences со скинами при каждом запуске
        val prefs = getSharedPreferences("game_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}