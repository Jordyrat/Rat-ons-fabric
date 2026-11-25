package com.ratons.data

/** Chat is the main input for dungeon and Kuudra state on SkyBlock; parsed text is enough. */
object ChatEvents {

    private val listeners = mutableListOf<(String) -> Unit>()

    fun register(listener: (String) -> Unit) {
        listeners.add(listener)
    }

    fun onMessage(raw: String) {
        val message = raw.trim()
        if (message.isEmpty()) return
        for (listener in listeners) {
            runCatching { listener(message) }
        }
    }
}
