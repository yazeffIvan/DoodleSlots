package com.example.doodleslots.presentation.quests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.doodleslots.domain.daily.DailyQuest
import com.example.doodleslots.presentation.theme.GamePalette

@Composable
fun DailyQuestsScreen(
    quests: List<DailyQuest>,
    streak: Int,
    onClaim: (String) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GamePalette.BgTop, GamePalette.BgBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🎯 ЗАДАНИЯ", style = MaterialTheme.typography.headlineMedium, color = GamePalette.NeonCyan)
                    Text("Обновляются каждый день", style = MaterialTheme.typography.labelSmall, color = GamePalette.TextMuted)
                }
                Surface(
                    color = if (streak >= 7) GamePalette.Gold else GamePalette.Surface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔥 СТРИК", style = MaterialTheme.typography.labelSmall, color = GamePalette.TextMuted)
                        Text(
                            "$streak дн.",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (streak >= 7) Color.Black else GamePalette.NeonGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quests) { q ->
                    QuestCard(quest = q, onClaim = { onClaim(q.id) })
                }
            }

            Spacer(Modifier.height(12.dp))

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

@Composable
private fun QuestCard(quest: DailyQuest, onClaim: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (quest.isReadyToClaim()) GamePalette.SurfaceHigh else GamePalette.Surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (quest.isReadyToClaim()) 8.dp else 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(quest.emoji, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        quest.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = GamePalette.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${quest.progress} / ${quest.target}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GamePalette.TextMuted
                    )
                }
                Surface(
                    color = GamePalette.Gold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "+${quest.reward} $",
                        color = GamePalette.Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { quest.progressRatio },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (quest.completed) GamePalette.NeonGreen else GamePalette.NeonCyan,
                trackColor = GamePalette.TextMuted.copy(alpha = 0.3f)
            )

            if (quest.isReadyToClaim()) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onClaim,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamePalette.NeonGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Text("🎁 ЗАБРАТЬ", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            } else if (quest.claimed) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "✓ Получено",
                    color = GamePalette.NeonGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}