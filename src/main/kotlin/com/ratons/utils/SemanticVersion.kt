package com.ratons.utils

class SemanticVersion private constructor(
    private val major: Int,
    private val minor: Int,
    private val extra: Int? = null,
) : Comparable<SemanticVersion> {

    override fun compareTo(other: SemanticVersion): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        return (extra ?: 0).compareTo(other.extra ?: 0)
    }

    override fun toString(): String = "$major.$minor" + (extra?.let { ".$it" } ?: "")

    companion object {
        fun of(version: String): SemanticVersion {
            val splits = version.trim().removePrefix("v").split('.', '-', '+')
            val first = splits.getOrNull(0)?.toIntOrNull() ?: 0
            val second = splits.getOrNull(1)?.toIntOrNull() ?: 0
            val third = splits.getOrNull(2)?.toIntOrNull()
            return SemanticVersion(first, second, third)
        }
    }
}
