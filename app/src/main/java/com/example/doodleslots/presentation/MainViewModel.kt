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
import com.example.doodleslots.domain.shop.ShopCatalog
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

    private val questManager = DailyQuestManager(dailyQuestRepository)

    // === Счётчик прокрутов для гарантированного выигрыша ===
    private var spinCount = 0

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
        _state.update { it.copy(screen = "menu", modeSelectionVisible = false, slotRewardVisible = false) }
    }

    fun showModeSelection() {
        _state.update { it.copy(modeSelectionVisible = true) }
    }

    fun hideModeSelection() {
        _state.update { it.copy(modeSelectionVisible = false) }
    }

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
            currentSkinId = s.currentSkin
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
            currentSkinId = s.currentSkin
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

    fun claimQuest(id: String) {
        val reward = questManager.claim(id)
        if (reward > 0) {
            val newMoney = _state.value.money + reward
            gameRepository.saveMoney(newMoney)
            _state.update { it.copy(money = newMoney) }
            refreshQuests()
        }
    }

    // ================ СЛОТЫ ================

    fun dismissSlotReward() {
        _state.update { it.copy(slotRewardVisible = false, slotWonSkins = emptyList()) }
    }

    private fun getSlotSymbols(ownedSkins: Set<String>): List<String> {
        val notOwnedSkinIds = ShopCatalog.droppableSkins
            .filter { !ownedSkins.contains(it.id) }
            .map { it.id }

        val symbols = mutableListOf<String>()
        symbols.addAll(notOwnedSkinIds)
        symbols.add("seven")
        while (symbols.size < 6) symbols.add("dummy")
        return symbols.shuffled()
    }

    fun spinSlots() {
        val s = _state.value
        if (s.slotSpinning) return

        val spinCost = 10
        if (s.money < spinCost) return

        val moneyAfterCost = s.money - spinCost
        gameRepository.saveMoney(moneyAfterCost)

        _state.update {
            it.copy(
                money = moneyAfterCost,
                slotSpinning = true,
                slotLastResult = null,
                slotWonSkins = emptyList(),
                slotRewardVisible = false,
                slotOffsets = listOf(0f, 0f, 0f)
            )
        }

        viewModelScope.launch {
            val availableSymbols = getSlotSymbols(s.ownedSkins)
            _state.update { it.copy(slotSymbols = availableSymbols) }

            // === Гарантированный выигрыш для первых 4 прокрутов ===
            val isForcedWin = spinCount < 4
            spinCount++

            val finalReels: List<Int> = if (isForcedWin) {
                // Сначала решаем: скин или 777?
                val notOwnedSkins = ShopCatalog.droppableSkins
                    .filter { !s.ownedSkins.contains(it.id) }
                    .map { it.id }

                val winSkin = if (notOwnedSkins.isNotEmpty() && Random.nextFloat() < 0.6f) {
                    notOwnedSkins.random()
                } else null

                if (winSkin != null) {
                    // Ищем индекс winSkin в availableSymbols и ставим его на все барабаны
                    val idx = availableSymbols.indexOf(winSkin).coerceAtLeast(0)
                    List(3) { idx }
                } else {
                    // 777
                    val sevenIdx = availableSymbols.indexOf("seven").coerceAtLeast(0)
                    List(3) { sevenIdx }
                }
            } else {
                // Обычный рандом
                List(3) { Random.nextInt(availableSymbols.size) }
            }

            val totalDuration = 1200f
            val frameDelay = 16L
            val frames = (totalDuration / frameDelay).toInt()
            val stopPercents = listOf(0.60f, 0.80f, 1.00f)
            val currentOffsets = MutableList(3) { 0f }
            val reelCount = availableSymbols.size

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
                        val currentMod = currentOffsets[i] % reelCount
                        val delta = ((stopOffset - currentMod) + reelCount) % reelCount
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
                            (offset.toInt() % reelCount + reelCount) % reelCount
                        }
                    )
                }
                delay(frameDelay)
            }

            val sym1 = availableSymbols[finalReels[0]]
            val sym2 = availableSymbols[finalReels[1]]
            val sym3 = availableSymbols[finalReels[2]]

            var wonSkins = emptyList<String>()
            var moneyReward = 0
            var resultText = "💀 Мимо"

            if (sym1 == sym2 && sym2 == sym3) {
                when {
                    sym1 == "seven" -> {
                        moneyReward = 500
                        resultText = "🎰 777! +$500 $"
                    }
                    sym1 == "dummy" -> {
                        resultText = "💨 Пустышки. Ничего."
                    }
                    else -> {
                        wonSkins = listOf(sym1)
                        resultText = "🎉 ВЫПАЛ СКИН!"
                    }
                }
            } else if (sym1 == sym2 || sym2 == sym3 || sym1 == sym3) {
                resultText = "😐 Две одинаковые. Ноль."
            }

            var newMoney = moneyAfterCost + moneyReward
            if (moneyReward > 0) gameRepository.saveMoney(newMoney)

            var newOwnedSkins = s.ownedSkins
            if (wonSkins.isNotEmpty()) {
                newOwnedSkins = s.ownedSkins + wonSkins
                gameRepository.saveOwnedSkins(newOwnedSkins)
            }

            _state.update {
                it.copy(
                    money = newMoney,
                    ownedSkins = newOwnedSkins,
                    slotSpinning = false,
                    slotReels = finalReels,
                    slotOffsets = finalReels.map { it.toFloat() },
                    slotLastResult = resultText,
                    slotWonSkins = wonSkins,
                    slotRewardVisible = wonSkins.isNotEmpty()
                )
            }
            refreshQuests()
        }
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
        _state.update { it.copy(money = newMoney, activeBoosts = newBoosts) }
        refreshQuests()
    }

    fun setSpeedMultiplier(value: Float) {
        gameRepository.saveSpeedMultiplier(value)
        _state.update { it.copy(speedMultiplier = value) }
    }

    /** Открыть все скины (для тестов) */
    fun unlockAllSkins() {
        val allSkinIds = ShopCatalog.allSkins.map { it.id }.toSet()
        gameRepository.saveOwnedSkins(allSkinIds)
        _state.update { it.copy(ownedSkins = allSkinIds) }
    }
}