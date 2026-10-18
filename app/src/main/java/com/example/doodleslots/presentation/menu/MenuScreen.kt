package com.example.doodleslots.presentation.menu

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doodleslots.presentation.theme.GamePalette

@Composable
fun MenuScreen(
    money: Int,
    bestScore: Int,
    hardcoreBest: Int,
    totalKills: Int,
    modeSelectionVisible: Boolean,
    onPlay: () -> Unit,
    onShowModeSelection: () -> Unit,
    onHideModeSelection: () -> Unit,
    onStartGame: (String) -> Unit,
    onShop: () -> Unit,
    onSettings: () -> Unit,
    onSlots: () -> Unit,
    onQuests: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "logo")
    val pulse by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GamePalette.BgTop, GamePalette.BgBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DOODLE", style = MaterialTheme.typography.displayLarge, color = GamePalette.NeonCyan, fontSize = 46.sp, modifier = Modifier.scale(pulse))
                Text("SLOTS", style = MaterialTheme.typography.displayLarge, color = GamePalette.NeonGreen, fontSize = 46.sp, modifier = Modifier.scale(pulse))
            }

            Surface(
                color = GamePalette.Surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    StatLine("БАЛАНС", "$money $", GamePalette.NeonGreen)
                    Spacer(Modifier.height(4.dp))
                    StatLine("🏆 РЕКОРД", "$bestScore м", GamePalette.Gold)
                    Spacer(Modifier.height(4.dp))
                    StatLine("🔥 HARDCORE", "$hardcoreBest м", GamePalette.NeonRed)
                    Spacer(Modifier.height(4.dp))
                    StatLine("💀 УБИЙСТВ", "$totalKills", GamePalette.NeonRed)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MenuButton("▶  ИГРАТЬ", GamePalette.NeonGreen, true, onShowModeSelection)
                MenuButton("🎰  СЛОТЫ", GamePalette.Gold, true, onSlots)
                MenuButton("🎯  ЗАДАНИЯ", GamePalette.NeonPurple, true, onQuests)
                MenuButton("🛒  МАГАЗИН", GamePalette.NeonCyan, false, onShop)
                MenuButton("⚙  НАСТРОЙКИ", GamePalette.NeonRed, false, onSettings)
            }

            Text("v1.0 • деген-эдишн", style = MaterialTheme.typography.labelSmall, color = GamePalette.TextMuted)
        }
    }

    // === Попап выбора режима ===
    if (modeSelectionVisible) {
        ModeSelectionDialog(
            onSelectNormal = { onStartGame("NORMAL") },
            onSelectHardcore = { onStartGame("HARDCORE") },
            onDismiss = onHideModeSelection
        )
    }
}

@Composable
private fun ModeSelectionDialog(
    onSelectNormal: () -> Unit,
    onSelectHardcore: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {},
            color = GamePalette.Surface,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "ВЫБЕРИ РЕЖИМ",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GamePalette.NeonCyan,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Классика или испытание?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GamePalette.TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // Обычный
                ModeCard(
                    emoji = "🎮",
                    title = "ОБЫЧНЫЙ",
                    description = "Классический режим с бонусами",
                    accent = GamePalette.NeonGreen,
                    onClick = onSelectNormal
                )

                Spacer(Modifier.height(12.dp))

                // Hardcore
                ModeCard(
                    emoji = "🔥",
                    title = "HARDCORE",
                    description = "Только опасные платформы. x2 деньги, отдельный рекорд",
                    accent = GamePalette.NeonRed,
                    onClick = onSelectHardcore
                )

                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Отмена", color = GamePalette.TextMuted)
                }
            }
        }
    }
}

@Composable
private fun ModeCard(
    emoji: String,
    title: String,
    description: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = GamePalette.SurfaceHigh,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 36.sp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = accent,
                    fontWeight = FontWeight.Black
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GamePalette.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = GamePalette.TextMuted)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MenuButton(text: String, color: Color, glow: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .alpha(if (glow) 1f else 0.85f)
            .shadow(if (glow) 16.dp else 6.dp, RoundedCornerShape(16.dp), spotColor = color),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.Black)
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
    }
}