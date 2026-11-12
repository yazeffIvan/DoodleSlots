package com.example.doodleslots.domain.shop
import com.example.doodleslots.R

enum class ShopCategory { SKIN, TRAIL, UPGRADE, PERK, BOOST }

data class ShopItem(
    val id: String,
    val category: ShopCategory,
    val name: String,
    val description: String,
    val emoji: String,
    val price: Int,
    val drawableRes: Int? = null // <--- картинка для скина
)

object ShopCatalog {

    val items: List<ShopItem> = listOf(
        // === Скины со стандартными эмодзи ===
        ShopItem("default", ShopCategory.SKIN, "Классический трейдер", "Обычный парень", "🧑‍💼", 0),
        ShopItem("skin_bull", ShopCategory.SKIN, "Бык с WSB", "Только вверх!", "🐂", 500),
        ShopItem("skin_pepe", ShopCategory.SKIN, "Крипто-лягушка", "Feels good man", "🐸", 1000),
        ShopItem("skin_astronaut", ShopCategory.SKIN, "Космонавт", "To the moon", "🚀", 2000),

        // === Новые скины с картинками pzdc ===
        ShopItem("skin_pzdc", ShopCategory.SKIN, "ляшишка", "Загадочный персонаж", "😈", 0, R.drawable.pzdc),
        ShopItem("skin_pzdc1", ShopCategory.SKIN, "ноги", "Загадочный персонаж", "😈", 0, R.drawable.pzdc1),
        ShopItem("skin_pzdc2", ShopCategory.SKIN, "PZDC 2", "Загадочный персонаж", "😈", 0, R.drawable.fire),
        ShopItem("skin_pzdc3", ShopCategory.SKIN, "одинокий волк", "Загадочный персонаж", "😈", 0, R.drawable.pzdc3),
        ShopItem("skin_pzdc4", ShopCategory.SKIN, "шерстяная", "Загадочный персонаж", "😈", 0, R.drawable.pzdc4),
        ShopItem("skin_pzdc5", ShopCategory.SKIN, "голем", "Загадочный персонаж", "😈", 0, R.drawable.golem),
        ShopItem("skin_pzdc6", ShopCategory.SKIN, "бро", "Загадочный персонаж", "😈", 0, R.drawable.pzdc6),

        // === Легендарные (только из слотов) ===
        ShopItem("skin_king", ShopCategory.SKIN, "Король крипты", "Только из слотов", "👑", -1),
        ShopItem("skin_dragon", ShopCategory.SKIN, "Дракон биржи", "Только из слотов", "🐉", -1),
        ShopItem("skin_robot", ShopCategory.SKIN, "Робот-трейдер", "Только из слотов", "🤖", -1),

        // === Шлейфы ===
        ShopItem("green", ShopCategory.TRAIL, "Зелёная свеча", "Стандартный след", "🟢", 0),
        ShopItem("gold", ShopCategory.TRAIL, "Золотой след", "Как у кита", "🟡", 300),
        ShopItem("purple", ShopCategory.TRAIL, "Неоновый фиолетовый", "Хайп-стиль", "🟣", 600),
        ShopItem("red", ShopCategory.TRAIL, "Маржин-колл", "Красный огонь", "🔴", 800),

        // === Апгрейды ===
        ShopItem("up_hp", ShopCategory.UPGRADE, "Доп. HP", "+1 сердце", "❤️", 500),
        ShopItem("up_firerate", ShopCategory.UPGRADE, "Скорострельность", "Быстрее стреляешь", "⚡", 400),
        ShopItem("up_damage", ShopCategory.UPGRADE, "Усиление урона", "Пробиваешь врагов", "💥", 600),
        ShopItem("up_magnet", ShopCategory.UPGRADE, "Магнит", "Монеты притягиваются", "🧲", 700),

        // === Перки ===
        ShopItem("perk_shield", ShopCategory.PERK, "Щит при старте", "5 сек неуязвимости", "🛡", 200),
        ShopItem("perk_doublejump", ShopCategory.PERK, "Двойной прыжок", "Второй прыжок в воздухе", "🦅", 300),
        ShopItem("perk_slowmo", ShopCategory.PERK, "Замедление", "При HP=1 всё замедляется", "⏳", 250),

        // === Бусты ===
        ShopItem("boost_x2money", ShopCategory.BOOST, "x2 монет", "Удвоение на 1 игру", "💰", 150),
        ShopItem("boost_leverage", ShopCategory.BOOST, "Плечо x20 старт", "Начинаешь с плечом", "⚡", 100),
        ShopItem("boost_saferespawn", ShopCategory.BOOST, "Безопасный респавн", "Не теряешь деньги 1 раз", "🕊", 200)
    )

    /** Все скины */
    val allSkins: List<ShopItem> = items.filter { it.category == ShopCategory.SKIN }

    /** Скины, которые могут выпасть в слотах (все, кроме default) */
    val droppableSkins: List<ShopItem> = allSkins.filter { it.id != "default" }

    fun byId(id: String): ShopItem? = items.firstOrNull { it.id == id }
    fun byCategory(cat: ShopCategory): List<ShopItem> = items.filter { it.category == cat }
    fun skinById(id: String): ShopItem? = allSkins.firstOrNull { it.id == id }
}