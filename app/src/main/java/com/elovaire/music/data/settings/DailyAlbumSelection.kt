package elovaire.music.droidbeauty.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import java.time.LocalDate
import kotlin.random.Random

private val albumOfDayDateKey = longPreferencesKey("album_of_day_date")
private val albumOfDayIdKey = longPreferencesKey("album_of_day_id")

internal suspend fun DataStore<Preferences>.albumOfDayId(
    albumIds: List<Long>,
    date: LocalDate,
    random: Random = Random.Default,
): Long? {
    if (albumIds.isEmpty()) return null
    val ids = albumIds.distinct()
    val day = date.toEpochDay()
    val preferences = edit { values ->
        val previousId = values[albumOfDayIdKey]
        if (values[albumOfDayDateKey] != day || previousId !in ids) {
            val candidates = if (ids.size > 1) ids.filterNot { it == previousId } else ids
            values[albumOfDayIdKey] = candidates.random(random)
            values[albumOfDayDateKey] = day
        }
    }
    return preferences[albumOfDayIdKey]
}
