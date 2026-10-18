package com.example.doodleslots.presentation

import com.example.doodleslots.domain.daily.DailyQuest

data class MainUiState(
    val screen: String = "menu",
    val money: Int = 100,
    val currentSkin: String = "default",
    val currentTrail: String = "green",
    val ownedSkins: Set<String> = setOf("default"),
    val ownedTrails: Set<String> = setOf("green"),
    val speedMultiplier: Float = 1.0f,
    val bestScore: Int = 0,
    val bestWave: Int = 0,
    val totalKills: Int = 0,
    val ownedUpgrades: Set<String> = emptySet(),
    val activePerks: Set<String> = emptySet(),
    val activeBoosts: Set<String> = emptySet(),

    val slotReels: List<Int> = listOf(0, 1, 2),
    val slotOffsets: List<Float> = listOf(0f, 0f, 0f),
    val slotSpinning: Boolean = false,
    val slotBet: Int = 10,
    val slotLastResult: String? = null,
    val slotLastWin: Int = 0,

    val dailyQuests: List<DailyQuest> = emptyList(),
    val dailyStreak: Int = 0,

    // === Режим ===
    val modeSelectionVisible: Boolean = false,
    val gameMode: String = "NORMAL",
    val hardcoreBest: Int = 0
)