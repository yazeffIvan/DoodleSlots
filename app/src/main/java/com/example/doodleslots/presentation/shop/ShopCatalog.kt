package com.example.doodleslots.domain.shop

/** Категория товара */
enum class ShopCategory { SKIN, TRAIL, UPGRADE, PERK, BOOST }

/** Один товар в магазине */
data class ShopItem(
    val id: String,
    val category: ShopCategory,
    val name: String,
    val description: String,
    val emoji: String,
    val price: Int,
    val maxLevel: Int = 1
)

/**
 * Каталог всех товаров. Добавить новый = добавить строку.
 */
object ShopCatalog {

    val items: List<ShopItem> = listOf(
        // --- Скины ---
        ShopItem("default", ShopCategory.SKIN, "Классический трейдер", "Обычный парень", "🧑‍💼", 0),
        ShopItem("skin_bull", ShopCategory.SKIN, "Бык с WSB", "Только вверх!", "🐂", 500),
        ShopItem("skin_pepe", ShopCategory.SKIN, "Крипто-лягушка", "Feels good man", "🐸", 1000),
        ShopItem("skin_astronaut", ShopCategory.SKIN, "Космонавт", "To the moon", "🚀", 2000),

        // --- Шлейфы ---
        ShopItem("green", ShopCategory.TRAIL, "Зелёная свеча", "Стандартный след", "🟢", 0),
        ShopItem("gold", ShopCategory.TRAIL, "Золотой след", "Как у кита", "🟡", 300),
        ShopItem("purple", ShopCategory.TRAIL, "Неоновый фиолетовый", "Хайп-стиль", "🟣", 600),
        ShopItem("red", ShopCategory.TRAIL, "Маржин-колл", "Красный огонь", "🔴", 800),

        // --- Апгрейды (постоянные) ---
        ShopItem("up_hp", ShopCategory.UPGRADE, "Доп. HP", "+1 сердце", "❤️", 500),
        ShopItem("up_firerate", ShopCategory.UPGRADE, "Скорострельность", "Быстрее стреляешь", "⚡", 400),
        ShopItem("up_damage", ShopCategory.UPGRADE, "Усиление урона", "Пробиваешь врагов", "💥", 600),
        ShopItem("up_magnet", ShopCategory.UPGRADE, "Магнит", "Монеты притягиваются", "🧲", 700),

        // --- Перки (на одну игру) ---
        ShopItem("perk_shield", ShopCategory.PERK, "Щит при старте", "5 сек неуязвимости", "🛡", 200),
        ShopItem("perk_doublejump", ShopCategory.PERK, "Двойной прыжок", "Второй прыжок в воздухе", "🦅", 300),
        ShopItem("perk_slowmo", ShopCategory.PERK, "Замедление", "При HP=1 всё замедляется", "⏳", 250),

        // --- Бусты (одноразовые) ---
        ShopItem("boost_x2money", ShopCategory.BOOST, "x2 монет", "Удвоение на 1 игру", "💰", 150),
        ShopItem("boost_leverage", ShopCategory.BOOST, "Плечо x20 старт", "Начинаешь с плечом", "⚡", 100),
        ShopItem("boost_saferespawn", ShopCategory.BOOST, "Безопасный респавн", "Не теряешь деньги 1 раз", "🕊", 200)
    )

    fun byId(id: String): ShopItem? = items.firstOrNull { it.id == id }

    fun byCategory(cat: ShopCategory): List<ShopItem> = items.filter { it.category == cat }
}