package com.itsash.dawa

import java.time.LocalDate

/** Pure data model of the dose engine. No Android types, so it is unit tested on the JVM. */
enum class Repeat { DAILY, WEEKDAYS, INTERVAL, AS_NEEDED }

data class Med(
    val id: Long,
    val name: String,
    val form: String = "tablet",
    val amount: Double = 1.0,
    val unit: String = "tablet",
    val food: String = "any",
    val personId: Long = 0,
    /** Minutes after local midnight, sorted. */
    val times: List<Int> = emptyList(),
    val repeat: Repeat = Repeat.DAILY,
    /** ISO weekdays 1=Mon..7=Sun, for [Repeat.WEEKDAYS]. */
    val days: Set<Int> = emptySet(),
    /** Every N days from [start], for [Repeat.INTERVAL]. */
    val every: Int = 1,
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val paused: Boolean = false,
    /** Units in the box when [stockAt] was set; null = not tracked. */
    val stock: Int? = null,
    val stockAt: Long = 0,
    /** Units used by one dose. */
    val perDose: Int = 1,
    val refillAt: Int = 5,
    val refillDays: Int = 0,
    val notes: String = "",
)

data class Person(val id: Long, val name: String)

data class Settings(
    val snoozeMin: Int = 10,
    val nagMin: Int = 10,
    val nagMax: Int = 3,
    val alarmStyle: Boolean = true,
    val sound: String? = null,
)

/** A scheduled dose. [key] is its identity: wall-clock date and time, stable across time zones. */
data class Dose(val medId: Long, val date: LocalDate, val minute: Int, val dueMs: Long) {
    val key: String get() = key(medId, date, minute)

    companion object {
        fun key(medId: Long, date: LocalDate, minute: Int): String =
            "$medId|$date|%02d%02d".format(minute / 60, minute % 60)
    }
}

data class LogEntry(
    val key: String,
    val medId: Long,
    val dueMs: Long,
    val atMs: Long,
    /** taken, skipped or missed. */
    val status: String,
    val qty: Int,
)

data class AlertDose(val key: String, val medId: Long, val dueMs: Long)

/**
 * One alert: the doses due in the same minute (or all late ones together).
 * [alerts] = how many times it has been raised (0 = scheduled test not yet raised).
 */
data class Alert(
    val gid: Long,
    val doses: List<AlertDose>,
    val alerts: Int,
    val nextAt: Long,
    val late: Boolean = false,
    val test: Boolean = false,
    val snoozes: Int = 0,
)

data class State(
    val meds: List<Med> = emptyList(),
    val settings: Settings = Settings(),
    val log: List<LogEntry> = emptyList(),
    val pending: List<Alert> = emptyList(),
    /** Everything due up to here has been looked at. 0 = never. */
    val cursor: Long = 0,
)
