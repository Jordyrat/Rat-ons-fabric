package com.ratons.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

/**
 * A point in time on the monotonic clock, which is what measurements of elapsed time should
 * use. Item data from the server carries wall-clock timestamps, so [epochMillis] converts one
 * to this clock rather than comparing the two directly.
 */
@JvmInline
value class SimpleTimeMark(private val nanos: Long) : Comparable<SimpleTimeMark> {

    /** How much time has passed since this mark, never negative. */
    fun passedSince(): Duration = (System.nanoTime() - nanos).coerceAtLeast(0).nanoseconds

    override fun compareTo(other: SimpleTimeMark): Int = nanos.compareTo(other.nanos)

    companion object {
        fun now(): SimpleTimeMark = SimpleTimeMark(System.nanoTime())

        fun epochMillis(epochMillis: Long): SimpleTimeMark {
            val age = (System.currentTimeMillis() - epochMillis).coerceAtLeast(0).milliseconds
            return SimpleTimeMark(System.nanoTime() - age.inWholeNanoseconds)
        }
    }
}
