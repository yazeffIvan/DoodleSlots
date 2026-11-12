package com.example.doodleslots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doodleslots.domain.engine.GameMode
import com.example.doodleslots.presentation.MainViewModel
import com.example.doodleslots.presentation.game.GameScreen
import com.example.doodleslots.presentation.menu.MenuScreen
import com.example.doodleslots.presentation.quests.DailyQuestsScreen
import com.example.doodleslots.presentation.settings.SettingsScreen
import com.example.doodleslots.presentation.shop.ShopScreen
import com.example.doodleslots.presentation.slots.SlotMachineScreen
import com.example.doodleslots.presentation.theme.DoodleSlotsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoodleSlotsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    when (state.screen) {
        "menu" -> MenuScreen(
            money = state.money,
            bestScore = state.bestScore,
            hardcoreBest = state.hardcoreBest,
            totalKills = state.totalKills,
            modeSelectionVisible = state.modeSelectionVisible,
            onPlay = viewModel::showModeSelection,
            onShowModeSelection = viewModel::showModeSelection,
            onHideModeSelection = viewModel::hideModeSelection,
            onStartGame = { modeStr ->
                val mode = if (modeStr == "HARDCORE") GameMode.HARDCORE else GameMode.NORMAL
                viewModel.startGame(mode)
            },
            onShop = viewModel::goToShop,
            onSettings = viewModel::goToSettings,
            onSlots = viewModel::goToSlots,
            onQuests = viewModel::goToQuests
        )
        "game" -> {
            val engine = viewModel.engine
            if (engine != null) {
                androidx.compose.runtime.key(engine) {
                    GameScreen(
                        engine = engine,
                        money = state.money,
                        bestScore = state.bestScore,
                        onBackToMenu = viewModel::goToMenu,
                        onRestart = viewModel::restart
                    )
                }
            }
        }
        "shop" -> ShopScreen(
            money = state.money,
            currentSkin = state.currentSkin,
            ownedSkins = state.ownedSkins,
            currentTrail = state.currentTrail,
            ownedTrails = state.ownedTrails,
            ownedUpgrades = state.ownedUpgrades,
            activePerks = state.activePerks,
            activeBoosts = state.activeBoosts,
            onSelectSkin = viewModel::selectSkin,
            onSelectTrail = viewModel::selectTrail,
            onBuyBoost = viewModel::buyBoost,
            onGoToSlots = viewModel::goToSlots,
            onBack = viewModel::goToMenu
        )
        "settings" -> SettingsScreen(
            speedMultiplier = state.speedMultiplier,
            onSpeedChange = viewModel::setSpeedMultiplier,
            onUnlockAllSkins = viewModel::unlockAllSkins,
            onBack = viewModel::goToMenu
        )
        "slots" -> SlotMachineScreen(
            money = state.money,
            reels = state.slotReels,
            offsets = state.slotOffsets,
            spinning = state.slotSpinning,
            slotSymbols = state.slotSymbols,
            lastResult = state.slotLastResult,
            wonSkins = state.slotWonSkins,
            rewardVisible = state.slotRewardVisible,
            onSpin = viewModel::spinSlots,
            onDismissReward = viewModel::dismissSlotReward,
            onBack = viewModel::goToMenu
        )
        "quests" -> DailyQuestsScreen(
            quests = state.dailyQuests,
            streak = state.dailyStreak,
            onClaim = viewModel::claimQuest,
            onBack = viewModel::goToMenu
        )
    }
}