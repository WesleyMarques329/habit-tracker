package com.wesley.nofap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wesley.nofap.data.levelAt
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth

private val CellSize = 13.dp
private val CellGap = 3.dp
private val ColumnWidth = CellSize + CellGap
private val HeaderHeight = 16.dp
private val LabelWidth = 26.dp

private val MonthNames = listOf(
    "jan", "fev", "mar", "abr", "mai", "jun",
    "jul", "ago", "set", "out", "nov", "dez",
)

/** Rótulos das linhas: só segunda, quarta e sexta, como no GitHub. */
private val RowLabels = listOf("", "seg", "", "qua", "", "sex", "")

@Composable
fun ContributionGrid(
    weeks: List<List<LocalDate?>>,
    marked: Set<LocalDate>,
    today: LocalDate,
    onToggle: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val segments = remember(weeks) { monthSegments(weeks) }

    LaunchedEffect(weeks.size) {
        snapshotFlow { scrollState.maxValue }.first { it > 0 }
        scrollState.scrollTo(scrollState.maxValue)
    }

    Row(modifier = modifier) {
        Column(modifier = Modifier.width(LabelWidth)) {
            Spacer(Modifier.height(HeaderHeight))
            RowLabels.forEach { label ->
                Box(
                    modifier = Modifier.height(ColumnWidth),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (label.isNotEmpty()) {
                        Text(label, fontSize = 9.sp, color = Palette.TextMuted)
                    }
                }
            }
        }

        Column(modifier = Modifier.horizontalScroll(scrollState)) {
            Row(modifier = Modifier.height(HeaderHeight)) {
                segments.forEach { segment ->
                    Box(modifier = Modifier.width(ColumnWidth * segment.weekCount)) {
                        if (segment.weekCount >= 3) {
                            Text(
                                text = MonthNames[segment.month.monthValue - 1],
                                fontSize = 9.sp,
                                color = Palette.TextMuted,
                            )
                        }
                    }
                }
            }

            Row {
                weeks.forEach { week ->
                    Column {
                        week.forEach { date ->
                            DayCell(
                                date = date,
                                level = date?.let { levelAt(it, marked) } ?: 0,
                                isToday = date == today,
                                onClick = { date?.let(onToggle) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    level: Int,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(3.dp)
    Box(
        modifier = Modifier
            .padding(end = CellGap, bottom = CellGap)
            .size(CellSize)
            .clip(shape)
            .background(if (date == null) Palette.Background else Palette.Levels[level])
            .then(if (isToday) Modifier.border(1.dp, Palette.TextPrimary, shape) else Modifier)
            .then(if (date == null) Modifier else Modifier.clickable(onClick = onClick)),
    )
}

@Composable
fun GridLegend(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("menos", fontSize = 10.sp, color = Palette.TextMuted)
        Spacer(Modifier.width(6.dp))
        Palette.Levels.forEach { color ->
            Box(
                modifier = Modifier
                    .padding(end = 3.dp)
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
        Spacer(Modifier.width(3.dp))
        Text("mais", fontSize = 10.sp, color = Palette.TextMuted)
    }
}

private data class MonthSegment(val month: YearMonth, val weekCount: Int)

/** Agrupa colunas consecutivas que pertencem ao mesmo mês, para o cabeçalho. */
private fun monthSegments(weeks: List<List<LocalDate?>>): List<MonthSegment> {
    val segments = mutableListOf<MonthSegment>()
    weeks.forEach { week ->
        val anchor = week.firstOrNull() ?: week.filterNotNull().firstOrNull() ?: return@forEach
        val month = YearMonth.from(anchor)
        val last = segments.lastOrNull()
        if (last != null && last.month == month) {
            segments[segments.lastIndex] = last.copy(weekCount = last.weekCount + 1)
        } else {
            segments.add(MonthSegment(month, 1))
        }
    }
    return segments
}
