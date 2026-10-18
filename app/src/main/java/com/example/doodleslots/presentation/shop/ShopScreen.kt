package com.example.doodleslots.presentation.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doodleslots.domain.shop.ShopCatalog
import com.example.doodleslots.domain.shop.ShopCategory
import com.example.doodleslots.domain.shop.ShopItem
import com.example.doodleslots.presentation.theme.GamePalette

@Composable
fun ShopScreen(
    money: Int,
    currentSkin: String,
    ownedSkins: Set<String>,
    currentTrail: String,
    ownedTrails: Set<String>,
    ownedUpgrades: Set<String>,
    activePerks: Set<String>,
    activeBoosts: Set<String>,
    onBuySkin: (String, Int) -> Unit,
    onSelectSkin: (String) -> Unit,
    onBuyTrail: (String, Int) -> Unit,
    onSelectTrail: (String) -> Unit,
    onBuyUpgrade: (String, Int) -> Unit,
    onBuyPerk: (String, Int) -> Unit,
    onBuyBoost: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(ShopCategory.SKIN) }

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
                    Text("🛒 МАГАЗИН", style = MaterialTheme.typography.headlineMedium, color = GamePalette.NeonCyan)
                    Text("Всё для настоящего дегена", style = MaterialTheme.typography.labelSmall, color = GamePalette.TextMuted)
                }
                Surface(color = GamePalette.Surface, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "$money $",
                        style = MaterialTheme.typography.titleLarge,
                        color = GamePalette.NeonGreen,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = GamePalette.Surface,
                contentColor = GamePalette.NeonCyan
            ) {
                TabCategory("🎨 Скины", ShopCategory.SKIN, selectedTab) { selectedTab = it }
                TabCategory("🌈 Шлейфы", ShopCategory.TRAIL, selectedTab) { selectedTab = it }
                TabCategory("⚡ Апгрейды", ShopCategory.UPGRADE, selectedTab) { selectedTab = it }
                TabCategory("🧪 Перки", ShopCategory.PERK, selectedTab) { selectedTab = it }
                TabCategory("🎁 Бусты", ShopCategory.BOOST, selectedTab) { selectedTab = it }
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ShopCatalog.byCategory(selectedTab)) { item ->
                    when (item.category) {
                        ShopCategory.SKIN -> ShopItemCard(
                            item = item,
                            isOwned = ownedSkins.contains(item.id),
                            isSelected = currentSkin == item.id,
                            canAfford = money >= item.price,
                            accent = GamePalette.NeonCyan,
                            onClick = {
                                if (ownedSkins.contains(item.id)) onSelectSkin(item.id)
                                else onBuySkin(item.id, item.price)
                            }
                        )
                        ShopCategory.TRAIL -> ShopItemCard(
                            item = item,
                            isOwned = ownedTrails.contains(item.id),
                            isSelected = currentTrail == item.id,
                            canAfford = money >= item.price,
                            accent = GamePalette.NeonGreen,
                            onClick = {
                                if (ownedTrails.contains(item.id)) onSelectTrail(item.id)
                                else onBuyTrail(item.id, item.price)
                            }
                        )
                        ShopCategory.UPGRADE -> ShopItemCard(
                            item = item,
                            isOwned = ownedUpgrades.contains(item.id),
                            isSelected = false,
                            canAfford = money >= item.price,
                            accent = GamePalette.Gold,
                            onClick = { onBuyUpgrade(item.id, item.price) }
                        )
                        ShopCategory.PERK -> ShopItemCard(
                            item = item,
                            isOwned = activePerks.contains(item.id),
                            isSelected = activePerks.contains(item.id),
                            canAfford = money >= item.price,
                            accent = GamePalette.NeonPurple,
                            onClick = {
                                if (!activePerks.contains(item.id)) onBuyPerk(item.id, item.price)
                            }
                        )
                        ShopCategory.BOOST -> ShopItemCard(
                            item = item,
                            isOwned = activeBoosts.contains(item.id),
                            isSelected = false,
                            canAfford = money >= item.price,
                            accent = GamePalette.NeonRed,
                            onClick = { onBuyBoost(item.id, item.price) }
                        )
                    }
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
                Text("← НАЗАД", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
private fun TabCategory(
    label: String,
    category: ShopCategory,
    selected: ShopCategory,
    onSelect: (ShopCategory) -> Unit
) {
    Tab(
        selected = selected == category,
        onClick = { onSelect(category) },
        text = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
    )
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    isOwned: Boolean,
    isSelected: Boolean,
    canAfford: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isSelected) GamePalette.SurfaceHigh else GamePalette.Surface,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = if (isSelected) 8.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.emoji, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = GamePalette.TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GamePalette.TextMuted
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            when {
                isSelected -> Badge("НАДЕТО", GamePalette.NeonCyan)
                isOwned && item.category == ShopCategory.UPGRADE -> Badge("КУПЛЕНО", GamePalette.NeonGreen)
                isOwned && item.category == ShopCategory.PERK -> Badge("АКТИВЕН", GamePalette.NeonPurple)
                isOwned && item.category == ShopCategory.BOOST -> Badge("ГОТОВ", GamePalette.NeonRed)
                isOwned -> Badge("ВЫБРАТЬ", GamePalette.NeonGreen)
                else -> {
                    Surface(
                        color = if (canAfford) accent else GamePalette.TextMuted,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "${item.price} $",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Surface(color = color, shape = RoundedCornerShape(10.dp)) {
        Text(
            text,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}