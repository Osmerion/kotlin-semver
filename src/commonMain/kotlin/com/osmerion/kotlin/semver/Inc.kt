package com.osmerion.kotlin.semver

/**
 * Determines by which identifier the given [Version] should be incremented.
 *
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.inc
 */
public enum class Inc {
    /**
     * Indicates that the [Version] should be incremented by its MAJOR number.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.inc
     */
    MAJOR,

    /**
     * Indicates that the [Version] should be incremented by its MINOR number.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.inc
     */
    MINOR,

    /**
     * Indicates that the [Version] should be incremented by its PATCH number.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.inc
     */
    PATCH,

    /**
     * Indicates that the [Version] should be incremented by its PRE-RELEASE identifier.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.inc
     */
    PRE_RELEASE
}
