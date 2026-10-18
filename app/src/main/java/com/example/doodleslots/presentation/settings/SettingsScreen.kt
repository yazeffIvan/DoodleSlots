package com.example.doodleslots.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doodleslots.presentation.theme.GamePalette

@Composable
fun SettingsScreen(
    speedMultiplier: Float,
    onSpeedChange: (Float) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GamePalette.BgTop, GamePalette.BgBottom)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Text(
                "⚙ НАСТРОЙКИ",
                style = MaterialTheme.typography.headlineMedium,
                color = GamePalette.NeonCyan
            )
            Text(
                "Скорость и управление",
                style = MaterialTheme.typography.labelSmall,
                color = GamePalette.TextMuted
            )

            Spacer(Modifier.height(24.dp))

            Surface(
                color = GamePalette.Surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "⚡ СКОРОСТЬ",
                        style = MaterialTheme.typography.titleMedium,
                        color = GamePalette.TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Текущая: ${"%.1f".format(speedMultiplier)}x",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GamePalette.NeonCyan
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1.0f, 1.5f, 2.0f, 3.0f).forEach { s ->
                            val selected = speedMultiplier == s
                            Button(
                                onClick = { onSpeedChange(s) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected)
                                        GamePalette.NeonCyan
                                    else
                                        GamePalette.SurfaceHigh,
                                    contentColor = if (selected) Color.Black else GamePalette.TextPrimary
                                )
                            ) {
                                Text(
                                    "${s}x",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
                Text("← НАЗАД", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
        }
    }
}