package com.example.bookeeper.util

import java.util.Calendar
import java.util.TimeZone

data class TimeRange(
    val startMillis: Long,
    val endExclusiveMillis: Long,
) {
    init {
        require(startMillis >= 0L)
        require(endExclusiveMillis > startMillis)
    }
}

fun startOfDay(
    timeMillis: Long,
    timeZone: TimeZone = TimeZone.getDefault(),
): Long = Calendar.getInstance(timeZone).apply {
    this.timeInMillis = timeMillis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun startOfNextDay(
    timeMillis: Long,
    timeZone: TimeZone = TimeZone.getDefault(),
): Long = Calendar.getInstance(timeZone).apply {
    this.timeInMillis = startOfDay(timeMillis, timeZone)
    add(Calendar.DAY_OF_MONTH, 1)
}.timeInMillis

fun monthRangeContaining(
    timeMillis: Long,
    timeZone: TimeZone = TimeZone.getDefault(),
): TimeRange {
    val calendar = Calendar.getInstance(timeZone).apply {
        this.timeInMillis = timeMillis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = calendar.timeInMillis
    calendar.add(Calendar.MONTH, 1)
    return TimeRange(start, calendar.timeInMillis)
}
