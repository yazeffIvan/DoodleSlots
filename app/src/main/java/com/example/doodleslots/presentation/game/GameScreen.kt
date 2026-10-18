package com.example.doodleslots.presentation.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.doodleslots.domain.engine.GameConfig
import com.example.doodleslots.domain.engine.GameEngine
import com.example.doodleslots.presentation.theme.GamePalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameScreen(
    engine: GameEngine,
    money: Int,
    bestScore: Int,
    onBackToMenu: () -> Unit,
    onRestart: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var hudScore by remember { mutableIntStateOf(engine.score) }
    var hudMoney by remember { mutableIntStateOf(engine.money) }
    var hudHp by remember { mutableIntStateOf(engine.hp) }
    var hudCombo by remember { mutableIntStateOf(engine.comboMultiplier) }
    var hudWave by remember { mutableIntStateOf(engine.waveLevel) }
    var hudDead by remember { mutableStateOf(engine.isDead) }
    var devPanelVisible by remember { mutableStateOf(false) }

    // Финальные значения замораживаем при смерти
    var finalScore by remember { mutableIntStateOf(0) }
    var finalMoney by remember { mutableIntStateOf(0) }
    var finalKills by remember { mutableIntStateOf(0) }
    var finalWave by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(modifier = Modifier.fillMaxSize().background(GamePalette.BgTop)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { e ->
                    if (hudDead) return@onKeyEvent false
                    when (e.key) {
                        Key.DirectionLeft -> {
                            when (e.type) {
                                KeyEventType.KeyDown -> { engine.inputLeft = true; engine.inputRight = false }
                                KeyEventType.KeyUp -> engine.inputLeft = false
                                else -> {}
                            }
                            true
                        }
                        Key.DirectionRight -> {
                            when (e.type) {
                                KeyEventType.KeyDown -> { engine.inputRight = true; engine.inputLeft = false }
                                KeyEventType.KeyUp -> engine.inputRight = false
                                else -> {}
                            }
                            true
                        }
                        Key.Spacebar -> {
                            if (e.type == KeyEventType.KeyDown) engine.shoot()
                            true
                        }
                        else -> false
                    }
                }
        ) {
            HudBar(
                score = hudScore,
                money = hudMoney,
                hp = hudHp,
                combo = hudCombo,
                wave = hudWave,
                onBack = {
                    engine.inputLeft = false
                    engine.inputRight = false
                    onBackToMenu()
                },
                onDev = { devPanelVisible = !devPanelVisible }
            )

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                if (!engine.isDead) {
                                    val leftHalf = down.position.x < size.width / 2f
                                    if (leftHalf) { engine.inputLeft = true; engine.inputRight = false }
                                    else { engine.inputRight = true; engine.inputLeft = false }
                                    waitForUpOrCancellation()
                                    engine.inputLeft = false
                                    engine.inputRight = false
                                }
                            }
                        },
                    factory = { ctx ->
                        GameView(ctx, engine).also { v ->
                            v.onHudUpdate = {
                                if (!engine.isDead) {
                                    hudScore = engine.score
                                    hudMoney = engine.money
                                    hudHp = engine.hp
                                    hudCombo = engine.comboMultiplier
                                    hudWave = engine.waveLevel
                                } else if (!hudDead) {
                                    // Только что умерли — фиксируем финальные значения
                                    finalScore = engine.score
                                    finalMoney = engine.money
                                    finalKills = engine.kills
                                    finalWave = engine.waveLevel
                                    hudDead = true
                                }
                            }
                        }
                    }
                )

                if (!hudDead) {
                    // Кнопка двойного прыжка (если куплен перк)
                    if (engine.activePerks.contains("perk_doublejump")) {
                        FloatingActionButton(
                            onClick = { engine.doubleJump() },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                                .size(64.dp),
                            containerColor = GamePalette.NeonPurple,
                            contentColor = Color.White
                        ) {
                            Text("🦅", fontSize = 26.sp)
                        }
                    }

                    FloatingActionButton(
                        onClick = { engine.shoot() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                            .size(72.dp),
                        containerColor = GamePalette.NeonRed,
                        contentColor = Color.White
                    ) {
                        Text("🔥", fontSize = 32.sp)
                    }
                }
            }
        }

        if (devPanelVisible && !hudDead) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.safeDrawing)) {
                DeveloperPanel(params = engine.params, onClose = { devPanelVisible = false })
            }
        }

        if (hudDead) {
            GameOverOverlay(
                score = finalScore,
                money = finalMoney,
                kills = finalKills,
                wave = finalWave,
                bestScore = bestScore,
                onRestart = onRestart,
                onMenu = onBackToMenu
            )
        }
    }
}

@Composable
private fun GameOverOverlay(
    score: Int,
    money: Int,
    kills: Int,
    wave: Int,
    bestScore: Int,
    onRestart: () -> Unit,
    onMenu: () -> Unit
) {
    val isNewRecord = score > bestScore

    // Пульсация для рекорда
    val infinite = rememberInfiniteTransition(label = "record")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (isNewRecord) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(
                Color(0xEE0A0E1A), Color(0xEE1A1F3A)
            )))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            color = GamePalette.Surface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "💀 ИГРА ОКОНЧЕНА",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GamePalette.NeonRed,
                    fontWeight = FontWeight.Black
                )

                if (isNewRecord) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "🏆 НОВЫЙ РЕКОРД!",
                        color = GamePalette.Gold,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        modifier = Modifier.scale(pulse)
                    )
                }

                Spacer(Modifier.height(24.dp))

                ResultRow("Счёт", "$score м", GamePalette.NeonCyan)
                ResultRow("Волна", "${wave + 1}", GamePalette.NeonPurple)
                ResultRow("Убийств", "$kills", GamePalette.NeonRed)
                ResultRow("Заработано", "$money $", GamePalette.NeonGreen)

                Spacer(Modifier.height(16.dp))
                Divider(color = GamePalette.TextMuted.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))

                ResultRow("🏆 Рекорд", "$bestScore м", GamePalette.Gold, small = true)

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamePalette.NeonGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Text("🔄 ИГРАТЬ СНОВА", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GamePalette.NeonCyan
                    )
                ) {
                    Text("← В МЕНЮ", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, color: Color, small: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = GamePalette.TextSecondary,
            fontSize = if (small) 12.sp else 14.sp
        )
        Text(
            value,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = if (small) 14.sp else 18.sp
        )
    }
}

@Composable
private fun HudBar(
    score: Int,
    money: Int,
    hp: Int,
    combo: Int,
    wave: Int,
    onBack: () -> Unit,
    onDev: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        color = GamePalette.Surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text("←", color = GamePalette.NeonCyan, fontSize = 22.sp) }
                    TextButton(onClick = onDev) { Text("🛠", fontSize = 18.sp) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatChip("PnL", "$score м", GamePalette.NeonCyan)
                    StatChip("BAL", "$money $", GamePalette.NeonGreen)
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    repeat(GameConfig.PLAYER_MAX_HP) { i ->
                        Text(
                            text = if (i < hp) "❤️" else "🤍",
                            fontSize = 18.sp
                        )
                    }
                }

                if (combo > 1) {
                    Text(
                        text = "x$combo COMBO",
                        color = GamePalette.Gold,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "УГРОЗА ${wave + 1}",
                    color = if (wave >= 3) GamePalette.NeonRed else GamePalette.NeonPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = GamePalette.TextMuted)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}