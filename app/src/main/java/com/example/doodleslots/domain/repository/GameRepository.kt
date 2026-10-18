package com.example.doodleslots.domain.repository

interface GameRepository {
    fun getMoney(): Int
    fun saveMoney(value: Int)

    fun getSpeedMultiplier(): Float
    fun saveSpeedMultiplier(value: Float)

    fun getCurrentSkin(): String
    fun saveCurrentSkin(skin: String)

    fun getCurrentTrail(): String
    fun saveCurrentTrail(trail: String)

    fun getOwnedSkins(): Set<String>
    fun saveOwnedSkins(skins: Set<String>)

    fun getOwnedTrails(): Set<String>
    fun saveOwnedTrails(trails: Set<String>)

    fun getThemeId(): String
    fun saveThemeId(id: String)

    fun isSoundEnabled(): Boolean
    fun saveSoundEnabled(v: Boolean)

    fun isVibrationEnabled(): Boolean
    fun saveVibrationEnabled(v: Boolean)

    fun resetProgress()

    fun getBestScore(): Int
    fun saveBestScore(score: Int)
    fun getBestWave(): Int
    fun saveBestWave(wave: Int)
    fun getTotalKills(): Int
    fun saveTotalKills(kills: Int)

    fun getOwnedUpgrades(): Set<String>
    fun saveOwnedUpgrades(upgrades: Set<String>)

    fun getActivePerks(): Set<String>
    fun saveActivePerks(perks: Set<String>)

    fun getActiveBoosts(): Set<String>
    fun saveActiveBoosts(boosts: Set<String>)
    fun getHardcoreBest(): Int
    fun saveHardcoreBest(score: Int)
}