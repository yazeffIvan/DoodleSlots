package com.example.doodleslots.presentation.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doodleslots.domain.engine.TunableParams

@Composable
fun DeveloperPanel(
    params: TunableParams,
    onClose: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    var tick by remember { mutableIntStateOf(0) }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        color = Color(0xCC000000),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🛠 Developer (обновлений: $tick)", color = Color.White, fontSize = 14.sp)
                Row {
                    TextButton(onClick = {
                        params.reset()
                        tick++
                        savedMessage = "Сброшено (не сохранено)"
                    }) { Text("Reset", fontSize = 12.sp) }

                    TextButton(onClick = {
                        params.save()
                        savedMessage = "✅ Сохранено!"
                    }) { Text("Save", fontSize = 12.sp, color = Color(0xFF00E5FF)) }

                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "▼" else "▲", fontSize = 12.sp)
                    }
                    TextButton(onClick = onClose) { Text("✕", fontSize = 12.sp) }
                }
            }

            savedMessage?.let {
                Text(it, color = Color(0xFF00E5FF), fontSize = 11.sp)
            }

            if (expanded) {
                ParamRow("Скорость X", params.moveSpeedBase, 0.5f, 0.5f, 40f) { params.moveSpeedBase = it; tick++ }
                ParamRow("Гравитация", params.gravity, 0.1f, 0.1f, 20f) { params.gravity = it; tick++ }
                ParamRow("Прыжок", params.jumpNormal, 1f, -120f, -5f) { params.jumpNormal = it; tick++ }
                ParamRow("Шаг платформ", params.platformStepY, 5f, 40f, 300f) { params.platformStepY = it; tick++ }
            }
        }
    }
}

@Composable
private fun ParamRow(
    name: String,
    value: Float,
    step: Float,
    min: Float,
    max: Float,
    onChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = Color.White, fontSize = 12.sp, modifier = Modifier.width(110.dp))
        IconButton(
            onClick = { onChange((value - step).coerceIn(min, max)) },
            modifier = Modifier.size(28.dp)
        ) { Text("−", color = Color.White, fontSize = 16.sp) }

        Text(
            "%.2f".format(value),
            color = Color(0xFF00E5FF),
            fontSize = 12.sp,
            modifier = Modifier.width(60.dp)
        )

        IconButton(
            onClick = { onChange((value + step).coerceIn(min, max)) },
            modifier = Modifier.size(28.dp)
        ) { Text("+", color = Color.White, fontSize = 16.sp) }
    }
}