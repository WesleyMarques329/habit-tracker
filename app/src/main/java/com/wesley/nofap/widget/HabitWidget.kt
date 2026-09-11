package com.wesley.nofap.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.wesley.nofap.MainActivity
import com.wesley.nofap.data.HabitRepository
import com.wesley.nofap.data.buildWeeks
import com.wesley.nofap.data.currentStreak
import com.wesley.nofap.data.levelAt
import java.time.LocalDate

/** Mesmas cores do app, no formato que o Glance entende. */
private val BackgroundColor = Color(0xFF0D1117)
private val Background = ColorProvider(BackgroundColor)
private val Accent = ColorProvider(Color(0xFF39D353))
private val Muted = ColorProvider(Color(0xFF8B949E))
private val Chip = ColorProvider(Color(0xFF21262D))

private val LevelColors = listOf(
    Color(0xFF21262D),
    Color(0xFF0E4429),
    Color(0xFF006D32),
    Color(0xFF26A641),
    Color(0xFF39D353),
)

private val Compact = DpSize(130.dp, 90.dp)
private val Wide = DpSize(250.dp, 100.dp)

private const val WIDGET_WEEKS = 13

object HabitWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(Compact, Wide))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = LocalDate.now()
        val marked = HabitRepository.markedDays(context)
        val streak = currentStreak(today, marked)
        val weeks = buildWeeks(today, WIDGET_WEEKS)

        provideContent {
            WidgetBody(
                streak = streak,
                doneToday = today in marked,
                weeks = weeks,
                marked = marked,
            )
        }
    }

    /** Chamado pelo app e pelo proprio widget depois de qualquer alteracao. */
    suspend fun refresh(context: Context) {
        updateAll(context)
    }
}

@Composable
private fun WidgetBody(
    streak: Int,
    doneToday: Boolean,
    weeks: List<List<LocalDate?>>,
    marked: Set<LocalDate>,
) {
    val wide = LocalSize.current.width >= Wide.width

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Background)
            .cornerRadius(18.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = streak.toString(),
                style = TextStyle(color = Accent, fontSize = 30.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                text = if (streak == 1) "DIA LIMPO" else "DIAS LIMPOS",
                style = TextStyle(color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Medium),
            )
            Spacer(GlanceModifier.height(8.dp))
            MarkButton(doneToday)
        }

        if (wide) {
            Spacer(GlanceModifier.width(14.dp))
            MiniGrid(weeks = weeks, marked = marked)
        }
    }
}

@Composable
private fun MarkButton(doneToday: Boolean) {
    Box(
        modifier = GlanceModifier
            .background(if (doneToday) Chip else Accent)
            .cornerRadius(10.dp)
            .clickable(actionRunCallback<ToggleTodayAction>()),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (doneToday) "feito hoje" else "marcar hoje",
            modifier = GlanceModifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = TextStyle(
                color = if (doneToday) Muted else Background,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun MiniGrid(weeks: List<List<LocalDate?>>, marked: Set<LocalDate>) {
    Row {
        weeks.forEach { week ->
            Column {
                week.forEach { date ->
                    val color = if (date == null) BackgroundColor else LevelColors[levelAt(date, marked)]
                    // Box externo so cria o espaco entre as celulas.
                    Box(
                        modifier = GlanceModifier.padding(end = 2.dp, bottom = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(9.dp)
                                .background(ColorProvider(color))
                                .cornerRadius(2.dp),
                            contentAlignment = Alignment.Center,
                        ) {}
                    }
                }
            }
        }
    }
}

class ToggleTodayAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        HabitRepository.toggle(context, LocalDate.now())
        HabitWidget.refresh(context)
    }
}
