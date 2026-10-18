package com.example.doodleslots.data

import android.content.Context
import com.example.doodleslots.domain.repository.GameRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val context: Context
) : GameRepository {

    private val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    override fun getMoney(): Int = prefs.getInt("money", 100)
    override fun saveMoney(value: Int) = prefs.edit().putInt("money", value).apply()

    override fun getSpeedMultiplier(): Float = prefs.getFloat("speed", 1.2f)
    override fun saveSpeedMultiplier(value: Float) = prefs.edit().putFloat("speed", value).apply()

    override fun getCurrentSkin(): String = prefs.getString("skin", "default") ?: "default"
    override fun saveCurrentSkin(skin: String) = prefs.edit().putString("skin", skin).apply()

    override fun getCurrentTrail(): String = prefs.getString("trail", "green") ?: "green"
    override fun saveCurrentTrail(trail: String) = prefs.edit().putString("trail", trail).apply()

    override fun getOwnedSkins(): Set<String> =
        prefs.getStringSet("owned_skins", setOf("default")) ?: setOf("default")
    override fun saveOwnedSkins(skins: Set<String>) =
        prefs.edit().putStringSet("owned_skins", skins).apply()

    override fun getOwnedTrails(): Set<String> =
        prefs.getStringSet("owned_trails", setOf("green")) ?: setOf("green")
    override fun saveOwnedTrails(trails: Set<String>) =
        prefs.edit().putStringSet("owned_trails", trails).apply()

    override fun getThemeId(): String = prefs.getString("theme", "neon") ?: "neon"
    override fun saveThemeId(id: String) = prefs.edit().putString("theme", id).apply()

    override fun isSoundEnabled(): Boolean = prefs.getBoolean("sound", true)
    override fun saveSoundEnabled(v: Boolean) = prefs.edit().putBoolean("sound", v).apply()

    override fun isVibrationEnabled(): Boolean = prefs.getBoolean("vibration", true)
    override fun saveVibrationEnabled(v: Boolean) = prefs.edit().putBoolean("vibration", v).apply()

    override fun resetProgress() {
        prefs.edit()
            .putInt("money", 100)
            .putString("skin", "default")
            .putString("trail", "green")
            .putStringSet("owned_skins", setOf("default"))
            .putStringSet("owned_trails", setOf("green"))
            .putStringSet("owned_upgrades", emptySet())
            .putStringSet("active_perks", emptySet())
            .putStringSet("active_boosts", emptySet())
            .apply()
    }

    override fun getBestScore(): Int = prefs.getInt("best_score", 0)
    override fun saveBestScore(score: Int) = prefs.edit().putInt("best_score", score).apply()

    override fun getBestWave(): Int = prefs.getInt("best_wave", 0)
    override fun saveBestWave(wave: Int) = prefs.edit().putInt("best_wave", wave).apply()

    override fun getTotalKills(): Int = prefs.getInt("total_kills", 0)
    override fun saveTotalKills(kills: Int) = prefs.edit().putInt("total_kills", kills).apply()

    override fun getOwnedUpgrades(): Set<String> =
        prefs.getStringSet("owned_upgrades", emptySet()) ?: emptySet()
    override fun saveOwnedUpgrades(upgrades: Set<String>) =
        prefs.edit().putStringSet("owned_upgrades", upgrades).apply()

    override fun getActivePerks(): Set<String> =
        prefs.getStringSet("active_perks", emptySet()) ?: emptySet()
    override fun saveActivePerks(perks: Set<String>) =
        prefs.edit().putStringSet("active_perks", perks).apply()

    override fun getActiveBoosts(): Set<String> =
        prefs.getStringSet("active_boosts", emptySet()) ?: emptySet()
    override fun saveActiveBoosts(boosts: Set<String>) =
        prefs.edit().putStringSet("active_boosts", boosts).apply()
    override fun getHardcoreBest(): Int = prefs.getInt("hardcore_best", 0)
    override fun saveHardcoreBest(score: Int) = prefs.edit().putInt("hardcore_best", score).apply()
}