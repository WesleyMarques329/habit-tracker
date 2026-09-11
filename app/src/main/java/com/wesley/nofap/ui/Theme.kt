package com.wesley.nofap.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Paleta fixa no escuro, no estilo do gráfico de contribuições do GitHub. */
object Palette {
    val Background = Color(0xFF0D1117)
    val Surface = Color(0xFF161B22)
    val Border = Color(0xFF30363D)
    val TextPrimary = Color(0xFFE6EDF3)
    val TextMuted = Color(0xFF8B949E)
    val Accent = Color(0xFF39D353)

    /** Índice 0 = dia vazio, 1..4 = intensidade crescente da sequência. */
    val Levels = listOf(
        Color(0xFF21262D),
        Color(0xFF0E4429),
        Color(0xFF006D32),
        Color(0xFF26A641),
        Color(0xFF39D353),
    )
}

private val DarkScheme = darkColorScheme(
    primary = Palette.Accent,
    onPrimary = Palette.Background,
    background = Palette.Background,
    onBackground = Palette.TextPrimary,
    surface = Palette.Surface,
    onSurface = Palette.TextPrimary,
)

@Composable
fun DisciplinaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, content = content)
}
