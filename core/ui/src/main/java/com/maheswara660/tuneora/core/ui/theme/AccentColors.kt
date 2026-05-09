package com.maheswara660.tuneora.core.ui.theme

import androidx.compose.ui.graphics.Color

data class AccentCombination(
    val primary: Color,
    val secondary: Color,
    val name: String
)

val CustomAccents = listOf(
    AccentCombination(Color(0xFF008080), Color(0xFF009688), "Default (Teal)"),
    AccentCombination(Color(0xFF0D47A1), Color(0xFF1565C0), "Midnight Blue"),
    AccentCombination(Color(0xFF1B5E20), Color(0xFF2E7D32), "Forest Green"),
    AccentCombination(Color(0xFFE65100), Color(0xFFEF6C00), "Sunset Orange"),
    AccentCombination(Color(0xFF4A148C), Color(0xFF6A1B9A), "Royal Violet"),
    AccentCombination(Color(0xFFB71C1C), Color(0xFFC62828), "Ruby Red"),
    AccentCombination(Color(0xFF006064), Color(0xFF00838F), "Ocean Deep"),
    AccentCombination(Color(0xFF880E4F), Color(0xFFAD1457), "Berry Punch"),
    AccentCombination(Color(0xFF7E5700), Color(0xFF7E5700), "Golden Sand"),
    AccentCombination(Color(0xFF3E2723), Color(0xFF4E342E), "Coffee Bean"),
    AccentCombination(Color(0xFFFF6F00), Color(0xFFFF8F00), "Amber Gold"),
    AccentCombination(Color(0xFF33691E), Color(0xFF558B2F), "Olive Moss"),
    AccentCombination(Color(0xFF006A6A), Color(0xFF6FF5F5), "Deep Teal"),
    AccentCombination(Color(0xFF4DDADA), Color(0xFF004F4F), "Bright Cyan")
)
