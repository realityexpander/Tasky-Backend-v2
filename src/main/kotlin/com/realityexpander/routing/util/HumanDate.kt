package com.realityexpander.routing.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * HumanDate - Utility class for converting Instants and EpochMillis to human-readable localized-time-zone date/time strings.
 *
 * * Add by right-clicking a variable, choose "View As"->"Create renderer" in debugger "Create Renderer"
 *    - Use this string:
 *  	  `new HumanDate(this).toTimeAgoStr();`
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.12
 * @param dateTimeInstant The Instant to be converted.
 * @param timeZone The time zone to be used for conversion. If null, the system default time zone will be used.
 */
class HumanDate @JvmOverloads constructor(
	private val dateTimeInstant: Instant,
	timeZone: ZoneId? = null
) {
	private val timeZone: ZoneId? // the local time zone for the user

	init {
		if (timeZone != null) {
			this.timeZone = timeZone
		} else {
			this.timeZone = ZoneId.systemDefault()
		}
	}

	companion object {
		val second = Duration.ofSeconds(1).toMillis()
		val minute = Duration.ofMinutes(1).toMillis()
		val hour = Duration.ofHours(1).toMillis()
		val day = Duration.ofDays(1).toMillis()
		val week = Duration.ofDays(7).toMillis()
		val month = Duration.ofDays(30).toMillis()
		val year = Duration.ofDays(365).toMillis()
	}

	constructor(epochMillis: Long) : this(Instant.ofEpochMilli(epochMillis), null)
	constructor(epochMillis: Long, timeZone: ZoneId?) : this(Instant.ofEpochMilli(epochMillis), timeZone)

	fun toDateTimeStr(): String {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
			.format(
				LocalDateTime.ofInstant(
					dateTimeInstant,
					timeZone
				)
			)
	}

	fun toDateStr(): String {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd")
			.format(
				LocalDateTime.ofInstant(
					dateTimeInstant,
					timeZone
				)
			)
	}

	fun toTimeStr(): String {
		return DateTimeFormatter.ofPattern("HH:mm:ss")
			.format(
				LocalDateTime.ofInstant(
					dateTimeInstant,
					timeZone
				)
			)
	}

	@JvmOverloads
	fun toTimeAgoStr(nowInstant: Instant? = Instant.now()): String {

		val nowInstant = nowInstant ?: Instant.now()
		val diff = dateTimeInstant.toEpochMilli() - nowInstant.toEpochMilli()

		///// Future /////

		if (diff > 0.seconds.inWholeMilliseconds && diff <= 5.seconds.inWholeMilliseconds) {
			return diff.milliseconds.inWholeMilliseconds.toString() + " milliseconds in the future"
		}
		if (diff > second && diff <= minute) {
			return (diff / second).toString() + " seconds in the future"
		}
		if (diff > minute && diff <= hour) {
			return (diff / minute).toString() + " minutes in the future"
		}
		if (diff > hour && diff <= day) {
			return (diff / hour).toString() + " hours in the future"
		}
		if (diff > day && diff <= month) {
			return (diff / day).toString() + " days in the future"
		}
		if( diff > month && diff <= week) {
			return (diff / day).toString() + " weeks in the future"
		}
		if (diff > month && diff <= year) {
			return (diff / month).toString() + " months in the future"
		}
		if (diff > year) {
			return (diff / year).toString() + " years (or more) in the future"
		}

		///// Past /////

		if (diff < 0 && diff >= -5.seconds.inWholeMilliseconds) {
			return (-diff).milliseconds.inWholeMilliseconds.toString() + " milliseconds ago"
		}
		if (diff < 0 && diff >= -minute) {
			return (-diff / second).toString() + " seconds ago"
		}
		if (diff < 0 && diff >= -hour) {
			return (-diff / minute).toString() + " minutes ago"
		}
		if (diff < 0 && diff >= -day) {
			return (-diff / hour).toString() + " hours ago"
		}
		if (diff < 0 && diff >= -week) {
			return (-diff / day).toString() + " days ago"
		}
		if (diff < 0 && diff >= -month) {
			return (-diff / week).toString() + " weeks ago"
		}
		if (diff < 0 && diff >= -year) {
			return (-diff / month).toString() + " months ago"
		}
		return (-diff / year).toString() + " years (or more) ago"
	}

	fun toTimeAgoStr(nowMillis: Long): String {
		return toTimeAgoStr(Instant.ofEpochMilli(nowMillis))
	}

	fun millis(): Long {
		return dateTimeInstant.toEpochMilli()
	}
}

fun main() {
	val now = Instant.now()
	val humanDate = HumanDate(now)
	println("Current DateTime: ${humanDate.toDateTimeStr()}")
	println("Current Date: ${humanDate.toDateStr()}")
	println("Current Time: ${humanDate.toTimeStr()}")
	println("Current Time Ago: ${humanDate.toTimeAgoStr()}, should be around '20 milliseconds ago'")
	println("")

	// 500 milliseconds in the future
	val futureMillis2 = now.toEpochMilli() + 500 // 500 milliseconds in the future
	val futureHumanDate2 = HumanDate(futureMillis2)
	println("Future DateTime: ${futureHumanDate2.toDateTimeStr()}")
	println("Future Date: ${futureHumanDate2.toDateStr()}")
	println("Future Time: ${futureHumanDate2.toTimeStr()}")
	println("Future Time Ago: ${futureHumanDate2.toTimeAgoStr()}, should be around '500 millisecond in the future'")
	println("")

	// 10 seconds in the past
	val pastMillis2 = now.toEpochMilli() - HumanDate.second * 10  // 10 seconds in the past
	val pastHumanDate2 = HumanDate(pastMillis2)
	println("Past DateTime: ${pastHumanDate2.toDateTimeStr()}")
	println("Past Date: ${pastHumanDate2.toDateStr()}")
	println("Past Time: ${pastHumanDate2.toTimeStr()}")
	println("Past Time Ago: ${pastHumanDate2.toTimeAgoStr()}, should be '10 seconds ago'")
	println("")

	// 10 seconds in the future
	val futureMillis1 = now.toEpochMilli() + HumanDate.second * 10  // 10 seconds in the future
	val futureHumanDate1 = HumanDate(futureMillis1)
	println("Future DateTime: ${futureHumanDate1.toDateTimeStr()}")
	println("Future Date: ${futureHumanDate1.toDateStr()}")
	println("Future Time: ${futureHumanDate1.toTimeStr()}")
	println("Future Time Ago: ${futureHumanDate1.toTimeAgoStr()}, should be '9 seconds in the future'")
	println("")

	val futureMillis = now.toEpochMilli() + HumanDate.day // 1 day in the future
	val futureHumanDate = HumanDate(futureMillis)
	println("Future DateTime: ${futureHumanDate.toDateTimeStr()}")
	println("Future Date: ${futureHumanDate.toDateStr()}")
	println("Future Time: ${futureHumanDate.toTimeStr()}")
	println("Future Time Ago: ${futureHumanDate.toTimeAgoStr()}, should be '23 hours in the future'")
	println("")

	val pastMillis = now.toEpochMilli() - HumanDate.day  // 1 day in the past
	val pastHumanDate = HumanDate(pastMillis)
	println("Past DateTime: ${pastHumanDate.toDateTimeStr()}")
	println("Past Date: ${pastHumanDate.toDateStr()}")
	println("Past Time: ${pastHumanDate.toTimeStr()}")
	println("Past Time Ago: ${pastHumanDate.toTimeAgoStr()}, should be '1 day ago'")
	println("")

	val pastMillis1 = now.toEpochMilli() - HumanDate.week // 1 week in the past
	val pastHumanDate1 = HumanDate(pastMillis1)
	println("Past DateTime: ${pastHumanDate1.toDateTimeStr()}")
	println("Past Date: ${pastHumanDate1.toDateStr()}")
	println("Past Time: ${pastHumanDate1.toTimeStr()}")
	println("Past Time Ago: ${pastHumanDate1.toTimeAgoStr()}, should be '1 week ago'")
	println("")

	// 1 month in the past
	val pastMillis3 = now.toEpochMilli() - HumanDate.month // 1 month in the past
	val pastHumanDate3 = HumanDate(pastMillis3)
	println("Past DateTime: ${pastHumanDate3.toDateTimeStr()}")
	println("Past Date: ${pastHumanDate3.toDateStr()}")
	println("Past Time: ${pastHumanDate3.toTimeStr()}")
	println("Past Time Ago: ${pastHumanDate3.toTimeAgoStr()}, should be '1 month ago'")
	println("")

	// 1 month in the future
	val futureMillis3 = now.toEpochMilli() + HumanDate.month + HumanDate.day // 1 month in the future
	val futureHumanDate3 = HumanDate(futureMillis3)
	println("Future DateTime: ${futureHumanDate3.toDateTimeStr()}")
	println("Future Date: ${futureHumanDate3.toDateStr()}")
	println("Future Time: ${futureHumanDate3.toTimeStr()}")
	println("Future Time Ago: ${futureHumanDate3.toTimeAgoStr()}, should be '1 month in the future'")
	println("")



}