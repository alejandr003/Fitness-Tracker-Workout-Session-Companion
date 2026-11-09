package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared

/**
 * Fitness Tracker & Workout Session Companion — "Pulse Circuit" palette tokens.
 *
 * Centralized, non-generic color tokens (ARGB hex as [Long]) shared across `mobile` and `wear`.
 * Subject: workout timing, effort, and live sync — energetic, high-contrast, readable in motion.
 *
 * Usage: modules construct Compose `Color(token)` in their theme layers.
 */
object FitnessPalette {
    // ── Brand accents ─────────────────────────────────────────────────────────
    /** Volt — primary action / active session / "go". */
    const val VOLT = 0xFFB8F02BL
    const val VOLT_DIM = 0xFF4A6700L
    const val ON_VOLT = 0xFF182100L
    const val VOLT_CONTAINER = 0xFF2D3F00L
    const val ON_VOLT_CONTAINER = 0xFFD4FF5CL

    /** Ember — secondary effort / pause / progress warmth. */
    const val EMBER = 0xFFFFB68EL
    const val EMBER_DIM = 0xFFA33E00L
    const val ON_EMBER = 0xFF532200L
    const val EMBER_CONTAINER = 0xFF6F2F00L
    const val ON_EMBER_CONTAINER = 0xFFFFDBCAL

    /** Tide — tertiary sync / metrics / recovery accent. */
    const val TIDE = 0xFF4FD1C5L
    const val TIDE_DIM = 0xFF006A63L
    const val ON_TIDE = 0xFF003733L
    const val TIDE_CONTAINER = 0xFF00504AL
    const val ON_TIDE_CONTAINER = 0xFF72F7EAL

    // ── Dark scheme surfaces (deep ink / slate) ───────────────────────────────
    const val DARK_BACKGROUND = 0xFF0F1419L
    const val DARK_ON_BACKGROUND = 0xFFE1E6EDL
    const val DARK_SURFACE = 0xFF171D23L
    const val DARK_ON_SURFACE = 0xFFE1E6EDL
    const val DARK_SURFACE_VARIANT = 0xFF3D4752L
    const val DARK_ON_SURFACE_VARIANT = 0xFFBCC7D4L
    const val DARK_OUTLINE = 0xFF8693A0L
    const val DARK_OUTLINE_VARIANT = 0xFF3D4752L

    // ── Light scheme surfaces (cool paper / graphite text) ───────────────────
    const val LIGHT_BACKGROUND = 0xFFF6F8FBL
    const val LIGHT_ON_BACKGROUND = 0xFF171D23L
    const val LIGHT_SURFACE = 0xFFF6F8FBL
    const val LIGHT_ON_SURFACE = 0xFF171D23L
    const val LIGHT_SURFACE_VARIANT = 0xFFDCE3EBL
    const val LIGHT_ON_SURFACE_VARIANT = 0xFF404A55L
    const val LIGHT_OUTLINE = 0xFF707B87L
    const val LIGHT_OUTLINE_VARIANT = 0xFFC0C9D3L

    // ── Semantic workout states (for UI mapping in later issues) ──────────────
    const val STATE_ACTIVE = VOLT
    const val STATE_PAUSED = EMBER
    const val STATE_IDLE = 0xFF8693A0L
    const val STATE_STOPPED = 0xFFFF5A6AL
    const val ON_STATE_STOPPED = 0xFF3F0008L
}
