package com.wesley.nofap.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.habitDataStore: DataStore<Preferences> by preferencesDataStore(name = "disciplina")

/**
 * Guarda apenas o conjunto de dias cumpridos, em formato ISO (yyyy-MM-dd).
 * Simples de propósito: o app e o widget leem a mesma fonte.
 */
object HabitRepository {

    private val MARKED_DAYS = stringSetPreferencesKey("marked_days")

    fun markedDaysFlow(context: Context): Flow<Set<LocalDate>> =
        context.applicationContext.habitDataStore.data.map { prefs -> prefs.toDates() }

    suspend fun markedDays(context: Context): Set<LocalDate> =
        markedDaysFlow(context).first()

    /** Marca o dia se estiver vazio, desmarca se já estiver marcado. Devolve o novo estado. */
    suspend fun toggle(context: Context, date: LocalDate): Boolean {
        var nowMarked = false
        context.applicationContext.habitDataStore.edit { prefs ->
            val days = prefs[MARKED_DAYS].orEmpty().toMutableSet()
            val key = date.toString()
            nowMarked = days.add(key)
            if (!nowMarked) days.remove(key)
            prefs[MARKED_DAYS] = days
        }
        return nowMarked
    }

    suspend fun clearAll(context: Context) {
        context.applicationContext.habitDataStore.edit { prefs ->
            prefs[MARKED_DAYS] = emptySet()
        }
    }

    private fun Preferences.toDates(): Set<LocalDate> =
        this[MARKED_DAYS].orEmpty().mapNotNullTo(mutableSetOf()) { raw ->
            runCatching { LocalDate.parse(raw) }.getOrNull()
        }
}
