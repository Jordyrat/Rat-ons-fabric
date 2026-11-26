package com.ratons.utils

import com.ratons.Ratons

/**
 * SkyHanni's rule for a client mod is that no failure in our own code may take the game down.
 * Every path the game calls into is wrapped in [guard], and both entry points report to the
 * player rather than only to the log, so a broken feature is visible in chat instead of silent.
 */
object ErrorManager {

    private const val REPEAT_INTERVAL_MILLIS = 30_000L
    private val lastReported = mutableMapOf<String, Long>()

    fun logErrorWithData(error: Throwable, userMessage: String, vararg extraData: Pair<String, Any?>) {
        val details = extraData.joinToString { (key, value) -> "$key=$value" }
        Ratons.logger.error("$userMessage $details", error)
        // A failure that repeats every tick would otherwise flood chat; one line per cause
        // per half minute is enough for the player to notice and report it.
        val now = System.nanoTime()
        val last = lastReported[userMessage]
        if (last == null || now - last > REPEAT_INTERVAL_MILLIS * 1_000_000L) {
            lastReported[userMessage] = now
            ChatUtils.userError("$userMessage Please report this with the log.")
        }
    }

    fun logErrorStateWithData(userMessage: String, internalMessage: String, vararg extraData: Pair<String, Any?>) {
        val details = extraData.joinToString { (key, value) -> "$key=$value" }
        Ratons.logger.error("$userMessage [$internalMessage] $details")
        ChatUtils.userError(userMessage)
    }

    /** Runs [block], turning any throwable into a reported error instead of a crash. */
    fun <T> guard(description: String, block: () -> T): T? = try {
        block()
    } catch (error: Throwable) {
        logErrorWithData(error, "Something went wrong while $description.")
        null
    }
}
