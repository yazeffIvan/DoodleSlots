package com.example.doodleslots.presentation.slots

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doodleslots.presentation.theme.GamePalette

private val SYMBOLS = listOf("🍒", "🍋", "🔔", "⭐", "💎", "7️⃣")
private val MULTIPLIERS = listOf("x2", "x2.5", "x3", "x4", "x4.5", "x5")
private val BETS = listOf(10, 50, 100, 500)

@Composable
fun SlotMachineScreen(
    money: Int,
    reels: List<Int>,
    offsets: List<Float>,
    spinning: Boolean,
    bet: Int,
    lastResult: String?,
    lastWin: Int,
    onBetChange: (Int) -> Unit,
    onSpin: () -> Unit,
    onBack: () -> Unit
) {
    val won = lastWin > bet
    val infinite = rememberInfiniteTransition(label = "win")
    val glowAlpha by infinite.animateFloat(
        initialValue = if (won) 0.4f else 0f,
        targetValue = if (won) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GamePalette.BgTop, GamePalette.BgBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Text("←", color = GamePalette.NeonCyan, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("🎰 СЛОТЫ", style = MaterialTheme.typography.headlineMedium, color = GamePalette.Gold, fontWeight = FontWeight.Black)
                        Text("Испытай удачу", style = MaterialTheme.typography.labelSmall, color = GamePalette.TextMuted)
                    }
                }
                Surface(color = GamePalette.Surface, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "$money $",
                        style = MaterialTheme.typography.titleLarge,
                        color = GamePalette.NeonGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier.shadow(
                    elevation = if (won) 24.dp else 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = if (won) GamePalette.Gold else GamePalette.NeonCyan,
                    ambientColor = if (won) GamePalette.Gold else GamePalette.NeonCyan
                )
            ) {
                Surface(
                    color = Color(0xFF08080C),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(
                        width = if (won) 3.dp else 1.dp,
                        color = if (won) GamePalette.Gold.copy(alpha = glowAlpha)
                        else GamePalette.NeonCyan.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in 0..2) {
                            SlotReel(
                                offset = offsets.getOrNull(i) ?: 0f,
                                spinning = spinning
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (lastResult != null) {
                Surface(
                    color = when {
                        lastWin > bet -> GamePalette.NeonGreen.copy(alpha = 0.2f)
                        lastWin == 0 -> GamePalette.NeonRed.copy(alpha = 0.15f)
                        else -> GamePalette.Surface
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        lastResult,
                        color = when {
                            lastWin > bet -> GamePalette.NeonGreen
                            lastWin == 0 -> GamePalette.NeonRed
                            else -> GamePalette.TextSecondary
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Выигрыш: $lastWin $",
                    color = if (lastWin > bet) GamePalette.Gold else GamePalette.TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Spacer(Modifier.height(60.dp))
            }

            Spacer(Modifier.height(16.dp))

            Text("СТАВКА", style = MaterialTheme.typography.labelSmall, color = GamePalette.TextMuted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BETS.forEach { b ->
                    Button(
                        onClick = { onBetChange(b) },
                        enabled = !spinning && b <= money,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (bet == b) GamePalette.Gold else GamePalette.SurfaceHigh,
                            contentColor = if (bet == b) Color.Black else GamePalette.TextPrimary
                        )
                    ) {
                        Text("$b", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onSpin,
                enabled = !spinning && bet <= money,
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamePalette.NeonRed,
                    contentColor = Color.White,
                    disabledContainerColor = GamePalette.SurfaceHigh,
                    disabledContentColor = GamePalette.TextMuted
                )
            ) {
                if (spinning) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    if (spinning) "КРУТИМ..." else "🎰 СПИН",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            Surface(
                color = GamePalette.Surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("КОЭФФИЦИЕНТЫ", style = MaterialTheme.typography.labelSmall, color = GamePalette.TextMuted)
                    Spacer(Modifier.height(8.dp))
                    SYMBOLS.forEachIndexed { i, sym ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$sym$sym$sym", fontSize = 16.sp, color = GamePalette.TextPrimary)
                            Text(MULTIPLIERS[i], fontSize = 16.sp, color = GamePalette.Gold, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Divider(color = GamePalette.TextMuted.copy(alpha = 0.3f))
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ничего", fontSize = 13.sp, color = GamePalette.TextSecondary)
                        Text("x0", fontSize = 13.sp, color = GamePalette.NeonRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamePalette.NeonCyan,
                    contentColor = Color.Black
                )
            ) {
                Text("← В МЕНЮ", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
        }
    }
}

/**
 * Один барабан.
 *
 * Структура:
 * - Верхняя ячейка: серый символ (сосед сверху).
 * - Центральная ячейка: яркий, крупный — это "выпавший" символ.
 * - Нижняя ячейка: серый символ (сосед снизу).
 */
@Composable
private fun SlotReel(offset: Float, spinning: Boolean) {
    val cellHeight = 60.dp

    val centerIndex = ((offset.toInt() % SYMBOLS.size) + SYMBOLS.size) % SYMBOLS.size
    val prevIndex = ((centerIndex - 1) % SYMBOLS.size + SYMBOLS.size) % SYMBOLS.size
    val nextIndex = (centerIndex + 1) % SYMBOLS.size

    val centerScale by animateFloatAsState(
        targetValue = if (spinning) 1.0f else 1.05f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "centerScale"
    )

    Box(
        modifier = Modifier
            .size(width = 90.dp, height = cellHeight * 3)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF050508),
                        Color(0xFF1A1A22),
                        Color(0xFF050508)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Верхняя ячейка — серая
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    SYMBOLS[prevIndex],
                    fontSize = 28.sp,
                    modifier = Modifier.alpha(0.35f)
                )
            }

            // Центральная — яркая
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    SYMBOLS[centerIndex],
                    fontSize = 42.sp,
                    modifier = Modifier.scale(centerScale)
                )
            }

            // Нижняя ячейка — серая
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    SYMBOLS[nextIndex],
                    fontSize = 28.sp,
                    modifier = Modifier.alpha(0.35f)
                )
            }
        }

        // Подсветка центральной ячейки
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cellHeight)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            GamePalette.NeonCyan.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Верхняя тень
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent)
                    )
                )
        )
        // Нижняя тень
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )
    }
}