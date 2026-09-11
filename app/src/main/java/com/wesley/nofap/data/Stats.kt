package com.wesley.nofap.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Quantos dias consecutivos terminam neste dia (0 se o dia não está marcado). */
fun runLengthAt(date: LocalDate, marked: Set<LocalDate>): Int {
    var length = 0
    var cursor = date
    while (cursor in marked) {
        length++
        cursor = cursor.minusDays(1)
    }
    return length
}

/**
 * Sequência atual. Hoje ainda não marcado não zera a contagem: vale a sequência
 * que termina ontem, para o dia em andamento não parecer uma recaída.
 */
fun currentStreak(today: LocalDate, marked: Set<LocalDate>): Int =
    if (today in marked) runLengthAt(today, marked) else runLengthAt(today.minusDays(1), marked)

fun bestStreak(marked: Set<LocalDate>): Int {
    var best = 0
    var run = 0
    var previous: LocalDate? = null
    for (day in marked.sorted()) {
        run = if (previous != null && previous.plusDays(1) == day) run + 1 else 1
        if (run > best) best = run
        previous = day
    }
    return best
}

/** Intensidade 0..4, no espírito do gráfico de contribuições do GitHub. */
fun levelAt(date: LocalDate, marked: Set<LocalDate>): Int = when (runLengthAt(date, marked)) {
    0 -> 0
    in 1..2 -> 1
    in 3..6 -> 2
    in 7..13 -> 3
    else -> 4
}

/**
 * Colunas de semanas (domingo a sábado) terminando na semana de [today].
 * Posições no futuro vêm como null para manter o alinhamento da grade.
 */
fun buildWeeks(today: LocalDate, weeks: Int): List<List<LocalDate?>> {
    val lastSaturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
    val firstSunday = lastSaturday
        .minusWeeks((weeks - 1).toLong())
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

    return (0 until weeks).map { week ->
        (0..6).map { dayOfWeek ->
            val date = firstSunday.plusWeeks(week.toLong()).plusDays(dayOfWeek.toLong())
            if (date.isAfter(today)) null else date
        }
    }
}
