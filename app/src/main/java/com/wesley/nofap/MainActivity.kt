package com.wesley.nofap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wesley.nofap.data.HabitRepository
import com.wesley.nofap.data.bestStreak
import com.wesley.nofap.data.buildWeeks
import com.wesley.nofap.data.currentStreak
import com.wesley.nofap.ui.ContributionGrid
import com.wesley.nofap.ui.DisciplinaTheme
import com.wesley.nofap.ui.GridLegend
import com.wesley.nofap.ui.Palette
import com.wesley.nofap.widget.HabitWidget
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val WEEKS_SHOWN = 53

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DisciplinaTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Palette.Background),
                ) {
                    HabitScreen()
                }
            }
        }
    }
}

@Composable
private fun HabitScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val markedFlow = remember(context) { HabitRepository.markedDaysFlow(context) }
    val marked by markedFlow.collectAsStateWithLifecycle(emptySet())

    var today by remember { mutableStateOf(LocalDate.now()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { today = LocalDate.now() }

    var askingReset by remember { mutableStateOf(false) }

    val weeks = remember(today) { buildWeeks(today, WEEKS_SHOWN) }
    val streak = currentStreak(today, marked)
    val record = bestStreak(marked)
    val doneToday = today in marked

    fun toggle(date: LocalDate) {
        scope.launch {
            HabitRepository.toggle(context, date)
            HabitWidget.refresh(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text(
            text = "DISCIPLINA",
            color = Palette.TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = streak.toString(),
                color = Palette.Accent,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (streak == 1) "dia limpo" else "dias limpos",
                color = Palette.TextMuted,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Recorde", "$record dias", Modifier.weight(1f))
            StatCard("Total", "${marked.size} dias", Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { toggle(today) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (doneToday) Palette.Surface else Palette.Accent,
                contentColor = if (doneToday) Palette.TextMuted else Palette.Background,
            ),
        ) {
            Text(
                text = if (doneToday) "Hoje marcado, toque para desmarcar" else "Marcar hoje",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Palette.Surface),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Ultimo ano",
                    color = Palette.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
                ContributionGrid(
                    weeks = weeks,
                    marked = marked,
                    today = today,
                    onToggle = { date -> toggle(date) },
                )
                Spacer(Modifier.height(8.dp))
                GridLegend()
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Toque em qualquer quadrado para marcar ou desmarcar aquele dia.",
            color = Palette.TextMuted,
            fontSize = 12.sp,
        )

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { askingReset = true }) {
            Text("Apagar todo o historico", color = Palette.TextMuted, fontSize = 12.sp)
        }
    }

    if (askingReset) {
        AlertDialog(
            onDismissRequest = { askingReset = false },
            containerColor = Palette.Surface,
            title = { Text("Apagar tudo?", color = Palette.TextPrimary) },
            text = {
                Text(
                    text = "Todos os dias marcados serao perdidos. Nao da para desfazer.",
                    color = Palette.TextMuted,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    askingReset = false
                    scope.launch {
                        HabitRepository.clearAll(context)
                        HabitWidget.refresh(context)
                    }
                }) { Text("Apagar", color = Palette.Accent) }
            },
            dismissButton = {
                TextButton(onClick = { askingReset = false }) {
                    Text("Cancelar", color = Palette.TextMuted)
                }
            },
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Palette.Surface),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = Palette.TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = Palette.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
