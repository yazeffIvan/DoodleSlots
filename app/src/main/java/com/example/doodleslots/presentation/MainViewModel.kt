package com.example.doodleslots.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doodleslots.domain.daily.DailyQuestManager
import com.example.doodleslots.domain.daily.QuestType
import com.example.doodleslots.domain.engine.GameEngine
import com.example.doodleslots.domain.engine.GameMode
import com.example.doodleslots.domain.engine.TunableParams
import com.example.doodleslots.domain.repository.DailyQuestRepository
import com.example.doodleslots.domain.repository.GameRepository
import com.example.doodleslots.domain.repository.TunableParamsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val paramsRepository: TunableParamsRepository,
    private val dailyQuestRepository: DailyQuestRepository
) : ViewModel() {

    private val symbolCount = 6
    private val questManager = DailyQuestManager(dailyQuestRepository)

    private val _state = MutableStateFlow(
        MainUiState(
            money = gameRepository.getMoney(),
            currentSkin = gameRepository.getCurrentSkin(),
            currentTrail = gameRepository.getCurrentTrail(),
            ownedSkins = gameRepository.getOwnedSkins(),
            ownedTrails = gameRepository.getOwnedTrails(),
            speedMultiplier = gameRepository.getSpeedMultiplier(),
            bestScore = gameRepository.getBestScore(),
            bestWave = gameRepository.getBestWave(),
            totalKills = gameRepository.getTotalKills(),
            ownedUpgrades = gameRepository.getOwnedUpgrades(),
            activePerks = gameRepository.getActivePerks(),
            activeBoosts = gameRepository.getActiveBoosts(),
            hardcoreBest = gameRepository.getHardcoreBest()
        )
    )
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    var engine: GameEngine? = null
        private set

    init {
        if (questManager.loadQuests().isEmpty()) {
            dailyQuestRepository.saveDay(-1L)
            dailyQuestRepository.saveQuestsRaw("")
        }
        questManager.ensureToday()
        refreshQuests()

        val bonus = questManager.checkStreakBonus()
        if (bonus > 0) {
            val newMoney = _state.value.money + bonus
            gameRepository.saveMoney(newMoney)
            _state.update { it.copy(money = newMoney) }
        }
    }

    private fun refreshQuests() {
        _state.update {
            it.copy(
                dailyQuests = questManager.loadQuests(),
                dailyStreak = questManager.getStreak()
            )
        }
    }

    // ================ НАВИГАЦИЯ ================

    fun goToMenu() {
        engine?.let { saveRunStats(it) }
        engine = null
        refreshQuests()
        _state.update { it.copy(screen = "menu", modeSelectionVisible = false) }
    }

    /** Нажатие на "Играть" — показываем попап выбора режима */
    fun showModeSelection() {
        _state.update { it.copy(modeSelectionVisible = true) }
    }

    fun hideModeSelection() {
        _state.update { it.copy(modeSelectionVisible = false) }
    }

    /** Старт игры в выбранном режиме */
    fun startGame(mode: GameMode) {
        val s = _state.value
        val params = TunableParams(paramsRepository)
        engine = GameEngine(
            params = params,
            ownedUpgrades = s.ownedUpgrades,
            activePerks = s.activePerks,
            activeBoosts = s.activeBoosts,
            mode = mode
        ).apply {
            money = s.money
            speedMultiplier = 1.0f
            trailColorId = s.currentTrail
        }
        if (s.activeBoosts.isNotEmpty()) {
            gameRepository.saveActiveBoosts(emptySet())
            _state.update { it.copy(activeBoosts = emptySet()) }
        }
        _state.update { it.copy(screen = "game", modeSelectionVisible = false, gameMode = mode.name) }
    }

    fun restart() {
        engine?.let { saveRunStats(it) }
        val s = _state.value
        val params = TunableParams(paramsRepository)
        val mode = if (s.gameMode == "HARDCORE") GameMode.HARDCORE else GameMode.NORMAL
        engine = GameEngine(
            params = params,
            ownedUpgrades = s.ownedUpgrades,
            activePerks = s.activePerks,
            activeBoosts = emptySet(),
            mode = mode
        ).apply {
            money = s.money
            speedMultiplier = 1.0f
            trailColorId = s.currentTrail
        }
        _state.update { it.copy(screen = "menu") }
        _state.update { it.copy(screen = "game") }
        refreshQuests()
    }

    private fun saveRunStats(e: GameEngine) {
        val s = _state.value
        val newBestScore = maxOf(s.bestScore, e.score)
        val newBestWave = maxOf(s.bestWave, e.waveLevel)
        val newTotalKills = s.totalKills + e.kills

        gameRepository.saveMoney(e.money)
        gameRepository.saveBestScore(newBestScore)
        gameRepository.saveBestWave(newBestWave)
        gameRepository.saveTotalKills(newTotalKills)

        // Отдельный рекорд для Hardcore
        if (e.mode == GameMode.HARDCORE && e.score > s.hardcoreBest) {
            gameRepository.saveHardcoreBest(e.score)
            _state.update { it.copy(hardcoreBest = e.score) }
        }

        questManager.addProgress(QuestType.KILL_ENEMIES, e.kills)
        questManager.addProgress(QuestType.SCORE_POINTS, e.score, absolute = true)
        questManager.addProgress(QuestType.REACH_WAVE, e.waveLevel + 1, absolute = true)

        _state.update {
            it.copy(
                money = e.money,
                bestScore = newBestScore,
                bestWave = newBestWave,
                totalKills = newTotalKills
            )
        }
        refreshQuests()
    }

    fun goToShop() { _state.update { it.copy(screen = "shop") } }
    fun goToSettings() { _state.update { it.copy(screen = "settings") } }
    fun goToSlots() { _state.update { it.copy(screen = "slots") } }
    fun goToQuests() { refreshQuests(); _state.update { it.copy(screen = "quests") } }

    // ================ ДЕЙЛИКИ ================

    fun claimQuest(id: String) {
        val reward = questManager.claim(id)
        if (reward > 0) {
            val newMoney = _state.value.money + reward
            gameRepository.saveMoney(newMoney)
            _state.update { it.copy(money = newMoney) }
            refreshQuests()
        }
    }

    // ================ СЛОТЫ (без изменений) ================

    fun setSlotBet(bet: Int) {
        if (_state.value.slotSpinning) return
        if (bet > _state.value.money) return
        _state.update { it.copy(slotBet = bet) }
    }

    fun spinSlots() {
        val s = _state.value
        if (s.slotSpinning) return
        if (s.slotBet > s.money) return

        questManager.addProgress(QuestType.SPIN_SLOTS, 1)

        val moneyAfterBet = s.money - s.slotBet
        gameRepository.saveMoney(moneyAfterBet)
        _state.update {
            it.copy(
                money = moneyAfterBet,
                slotSpinning = true,
                slotLastResult = null,
                slotLastWin = 0,
                slotOffsets = listOf(0f, 0f, 0f)
            )
        }

        viewModelScope.launch {
            val finalReels = List(3) { Random.nextInt(symbolCount) }
            val totalDuration = 1200f
            val frameDelay = 16L
            val frames = (totalDuration / frameDelay).toInt()
            val stopPercents = listOf(0.60f, 0.80f, 1.00f)
            val currentOffsets = MutableList(3) { 0f }

            for (frame in 0..frames) {
                val progress = frame.toFloat() / frames
                for (i in 0..2) {
                    if (progress < stopPercents[i]) {
                        val localProgress = progress / stopPercents[i]
                        val speed = 1f - localProgress.pow(2f)
                        val baseSpeed = 0.35f
                        currentOffsets[i] += baseSpeed * (speed + 0.15f)
                    } else {
                        val stopOffset = finalReels[i].toFloat()
                        val currentMod = currentOffsets[i] % symbolCount
                        val delta = ((stopOffset - currentMod) + symbolCount) % symbolCount
                        val remainingFrames = frames - frame
                        if (remainingFrames > 0) {
                            currentOffsets[i] += delta / remainingFrames.toFloat()
                        } else {
                            currentOffsets[i] = finalReels[i].toFloat()
                        }
                    }
                }
                _state.update {
                    it.copy(
                        slotOffsets = currentOffsets.toList(),
                        slotReels = currentOffsets.map { offset ->
                            (offset.toInt() % symbolCount + symbolCount) % symbolCount
                        }
                    )
                }
                delay(frameDelay)
            }

            val multiplier = calculateMultiplier(finalReels)
            val win = (s.slotBet * multiplier).toInt()
            val newMoney = moneyAfterBet + win
            gameRepository.saveMoney(newMoney)

            if (multiplier >= 3f) {
                questManager.addProgress(QuestType.WIN_SLOTS, 1)
            }

            val resultText = when {
                multiplier >= 5f -> "🎉 ДЖЕКПОТ x5!"
                multiplier >= 4f -> "💎 БОМБА x$multiplier"
                multiplier >= 3f -> "🔥 КРУТО x$multiplier"
                multiplier >= 2f -> "✨ ХОРОШО x$multiplier"
                multiplier >= 1f -> "🙂 Возврат x1"
                else -> "💀 Мимо"
            }

            _state.update {
                it.copy(
                    money = newMoney,
                    slotSpinning = false,
                    slotReels = finalReels,
                    slotOffsets = finalReels.map { it.toFloat() },
                    slotLastResult = resultText,
                    slotLastWin = win
                )
            }
            refreshQuests()
        }
    }

    private fun calculateMultiplier(reels: List<Int>): Float {
        val a = reels[0]; val b = reels[1]; val c = reels[2]
        if (a == b && b == c) {
            return when (a) {
                0 -> 2f; 1 -> 2.5f; 2 -> 3f; 3 -> 4f; 4 -> 4.5f; 5 -> 5f; else -> 0f
            }
        }
        if (a == b || b == c || a == c) return 1f
        return 0f
    }

    // ================ МАГАЗИН ================

    fun buySkin(id: String, price: Int) {
        val s = _state.value
        if (s.money < price) return
        val newSkins = s.ownedSkins + id
        val newMoney = s.money - price
        gameRepository.saveMoney(newMoney)
        gameRepository.saveOwnedSkins(newSkins)
        gameRepository.saveCurrentSkin(id)
        questManager.addProgress(QuestType.BUY_ITEM, 1)
        _state.update { it.copy(money = newMoney, ownedSkins = newSkins, currentSkin = id) }
        refreshQuests()
    }

    fun selectSkin(id: String) {
        if (!_state.value.ownedSkins.contains(id)) return
        gameRepository.saveCurrentSkin(id)
        _state.update { it.copy(currentSkin = id) }
    }

    fun buyTrail(id: String, price: Int) {
        val s = _state.value
        if (s.money < price) return
        val newTrails = s.ownedTrails + id
        val newMoney = s.money - price
        gameRepository.saveMoney(newMoney)
        gameRepository.saveOwnedTrails(newTrails)
        gameRepository.saveCurrentTrail(id)
        questManager.addProgress(QuestType.BUY_ITEM, 1)
        _state.update { it.copy(money = newMoney, ownedTrails = newTrails, currentTrail = id) }
        refreshQuests()
    }

    fun selectTrail(id: String) {
        if (!_state.value.ownedTrails.contains(id)) return
        gameRepository.saveCurrentTrail(id)
        _state.update { it.copy(currentTrail = id) }
    }

    fun buyUpgrade(id: String, price: Int) {
        val s = _state.value
        if (s.money < price) return
        if (s.ownedUpgrades.contains(id)) return
        val newUpgrades = s.ownedUpgrades + id
        val newMoney = s.money - price
        gameRepository.saveMoney(newMoney)
        gameRepository.saveOwnedUpgrades(newUpgrades)
        questManager.addProgress(QuestType.BUY_ITEM, 1)
        _state.update { it.copy(money = newMoney, ownedUpgrades = newUpgrades) }
        refreshQuests()
    }

    fun buyPerk(id: String, price: Int) {
        val s = _state.value
        if (s.money < price) return
        if (s.activePerks.contains(id)) return
        val newPerks = s.activePerks + id
        val newMoney = s.money - price
        gameRepository.saveMoney(newMoney)
        gameRepository.saveActivePerks(newPerks)
        questManager.addProgress(QuestType.BUY_ITEM, 1)
        _state.update { it.copy(money = newMoney, activePerks = newPerks) }
        refreshQuests()
    }

    fun togglePerk(id: String) {
        val s = _state.value
        val newPerks = if (s.activePerks.contains(id)) s.activePerks - id else s.activePerks + id
        gameRepository.saveActivePerks(newPerks)
        _state.update { it.copy(activePerks = newPerks) }
    }

    fun buyBoost(id: String, price: Int) {
        val s = _state.value
        if (s.money < price) return
        if (s.activeBoosts.contains(id)) return
        val newBoosts = s.activeBoosts + id
        val newMoney = s.money - price
        gameRepository.saveMoney(newMoney)
        gameRepository.saveActiveBoosts(newBoosts)
        questManager.addProgress(QuestType.BUY_ITEM, 1)
        _state.update { it.copy(money = newMoney, activeBoosts = newBoosts) }
        refreshQuests()
    }

    fun setSpeedMultiplier(value: Float) {
        gameRepository.saveSpeedMultiplier(value)
        _state.update { it.copy(speedMultiplier = value) }
    }
}