package com.example.bookeeper.util

import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class DateRangesTest {
    private val utc = TimeZone.getTimeZone("UTC")

    @Test
    fun monthRange_usesMonthStartAndNextMonthExclusive() {
        val range = monthRangeContaining(timestamp(2026, Calendar.AUGUST, 12, 15), utc)

        assertEquals(timestamp(2026, Calendar.AUGUST, 1, 0), range.startMillis)
        assertEquals(timestamp(2026, Calendar.SEPTEMBER, 1, 0), range.endExclusiveMillis)
    }

    @Test
    fun nextDay_isExclusiveBoundaryAfterSelectedDate() {
        val selected = timestamp(2026, Calendar.AUGUST, 12, 18)

        assertEquals(timestamp(2026, Calendar.AUGUST, 13, 0), startOfNextDay(selected, utc))
    }

    private fun timestamp(year: Int, month: Int, day: Int, hour: Int): Long =
        Calendar.getInstance(utc).apply {
            clear()
            set(year, month, day, hour, 0, 0)
        }.timeInMillis
}
