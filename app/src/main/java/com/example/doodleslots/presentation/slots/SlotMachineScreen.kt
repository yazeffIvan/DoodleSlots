package com.example.doodleslots.presentation.slots

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doodleslots.domain.shop.ShopCatalog
import com.example.doodleslots.presentation.theme.GamePalette

@Composable
fun SlotMachineScreen(
    money: Int,
    reels: List<Int>,
    offsets: List<Float>,
    spinning: Boolean,
    slotSymbols: List<String>,
    lastResult: String?,
    wonSkins: List<String>,
    rewardVisible: Boolean,
    onSpin: () -> Unit,
    onDismissReward: () -> Unit,
    onBack: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(600),
            androidx.compose.animation.core.RepeatMode.Reverse
        ),
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
                        Text("Собери три одинаковые!", style = MaterialTheme.typography.labelSmall, color = GamePalette.TextMuted)
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

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier.shadow(12.dp, RoundedCornerShape(20.dp), spotColor = GamePalette.Gold)
            ) {
                Surface(
                    color = Color(0xFF08080C),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(2.dp, GamePalette.Gold.copy(alpha = glowAlpha), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in 0..2) {
                            SlotReel(
                                offset = offsets.getOrNull(i) ?: 0f,
                                spinning = spinning,
                                symbols = slotSymbols
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (lastResult != null) {
                Surface(color = GamePalette.Surface, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        lastResult,
                        color = GamePalette.Gold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            } else {
                Spacer(Modifier.height(50.dp))
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "СТОИМОСТЬ: 10 $",
                color = GamePalette.TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onSpin,
                enabled = !spinning && money >= 10,
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
                    if (spinning) "КРУТИМ..." else "🎰 КРУТИТЬ",
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
                    Text("КАК ИГРАТЬ", style = MaterialTheme.typography.labelSmall, color = GamePalette.TextMuted)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "• Три одинаковых скина — забираешь скин\n• Три семёрки — +500 монет\n• Две одинаковые — ничего\n• Пустышки — ничего",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GamePalette.TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.weight(1f))
        }

        if (rewardVisible && wonSkins.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    color = GamePalette.Surface,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🎉 ВЫИГРЫШ!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GamePalette.Gold,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(16.dp))

                        for (skinId in wonSkins) {
                            val skin = ShopCatalog.skinById(skinId)
                            if (skin != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(64.dp),
                                        color = GamePalette.SurfaceHigh,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            SkinImage(skinId = skinId, size = 64.dp)
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            skin.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = GamePalette.TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Добавлен в коллекцию",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = GamePalette.NeonGreen
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = onDismissReward,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GamePalette.NeonGreen,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("ЗАБРАТЬ", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkinImage(skinId: String, size: androidx.compose.ui.unit.Dp) {
    when (skinId) {
        "seven" -> {
            Text("7️⃣", fontSize = (size.value * 0.7).sp)
        }
        "dummy" -> {
            Text("❓", fontSize = (size.value * 0.7).sp, color = GamePalette.TextMuted)
        }
        else -> {
            val skin = ShopCatalog.skinById(skinId)
            if (skin != null && skin.drawableRes != null) {
                Image(
                    painter = painterResource(id = skin.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
            } else if (skin != null) {
                Text(skin.emoji, fontSize = (size.value * 0.7).sp)
            } else {
                Text("❓", fontSize = (size.value * 0.7).sp, color = GamePalette.TextMuted)
            }
        }
    }
}

@Composable
private fun SlotReel(offset: Float, spinning: Boolean, symbols: List<String>) {
    val cellHeight = 60.dp
    val totalSymbols = symbols.size.coerceAtLeast(1)
    val centerIndex = ((offset.toInt() % totalSymbols) + totalSymbols) % totalSymbols

    val centerScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (spinning) 1.0f else 1.05f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.5f, stiffness = 400f
        ),
        label = "centerScale"
    )

    val prevIndex = ((centerIndex - 1) % totalSymbols + totalSymbols) % totalSymbols
    val nextIndex = (centerIndex + 1) % totalSymbols

    Box(
        modifier = Modifier
            .size(width = 90.dp, height = cellHeight * 3)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050508), Color(0xFF1A1A22), Color(0xFF050508))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(cellHeight),
                contentAlignment = Alignment.Center
            ) {
                val symbolId = symbols.getOrElse(prevIndex) { "dummy" }
                Box(modifier = Modifier.alpha(0.35f)) {
                    SkinImage(skinId = symbolId, size = 48.dp)
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(cellHeight),
                contentAlignment = Alignment.Center
            ) {
                val symbolId = symbols.getOrElse(centerIndex) { "dummy" }
                Box(modifier = Modifier.scale(centerScale)) {
                    SkinImage(skinId = symbolId, size = 52.dp)
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(cellHeight),
                contentAlignment = Alignment.Center
            ) {
                val symbolId = symbols.getOrElse(nextIndex) { "dummy" }
                Box(modifier = Modifier.alpha(0.35f)) {
                    SkinImage(skinId = symbolId, size = 48.dp)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cellHeight)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, GamePalette.NeonCyan.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(16.dp).align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent)))
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(16.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))))
        )
    }
}