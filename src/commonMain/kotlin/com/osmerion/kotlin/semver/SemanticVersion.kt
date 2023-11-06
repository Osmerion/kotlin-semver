/*
 * Copyright (c) 2022 Peter Csajtai
 * Copyright (c) 2023 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.osmerion.kotlin.semver

import com.osmerion.kotlin.semver.internal.Patterns
import com.osmerion.kotlin.semver.serializers.VersionSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * This class describes a semantic version and related operations following the semver 2.0.0 specification.
 * Instances of this class are immutable, which makes them thread-safe.
 *
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.explode
 *
 * @since   0.1.0
 */
@Serializable(with = VersionSerializer::class)
public class SemanticVersion private constructor(
    /** The MAJOR number of the version. */
    public val major: Int,

    /** The MINOR number of the version. */
    public val minor: Int,

    /** The PATCH number of the version. */
    public val patch: Int,

    internal val parsedPreRelease: PreRelease? = null,

    /** The BUILD metadata of the version. */
    public val buildMetadata: String? = null
) : Comparable<SemanticVersion> {

    /**
     * Constructs a semantic version from the given arguments following the pattern:
     * <[major]>.<[minor]>.<[patch]>-<[preRelease]>+<[buildMetadata]>
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.construct
     */
    public constructor(
        major: Int = 0,
        minor: Int = 0,
        patch: Int = 0,
        preRelease: String? = null,
        buildMetadata: String? = null
    ) : this(major, minor, patch, preRelease?.let(PreRelease.Companion::invoke), buildMetadata)

    init {
        when {
            major < 0 -> throw VersionFormatException("The major number must be >= 0.")
            minor < 0 -> throw VersionFormatException("The minor number must be >= 0.")
            patch < 0 -> throw VersionFormatException("The patch number must be >= 0.")
        }
    }

    /** The PRE-RELEASE identifier of the version. */
    public val preRelease: String? = parsedPreRelease?.toString()

    /**
     * Determines whether the version is pre-release or not.
     */
    public val isPreRelease: Boolean = parsedPreRelease != null

    /**
     * Determines whether the version is considered stable or not.
     * Stable versions have a positive major number and no pre-release identifier.
     */
    public val isStable: Boolean = major > 0 && parsedPreRelease == null

    /**
     * Constructs a copy of the [SemanticVersion]. The copied object's properties can be altered with the optional parameters.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.copy
     */
    public fun copy(
        major: Int = this.major,
        minor: Int = this.minor,
        patch: Int = this.patch,
        preRelease: String? = this.preRelease,
        buildMetadata: String? = this.buildMetadata
    ): SemanticVersion = SemanticVersion(major, minor, patch, preRelease, buildMetadata)

    public override fun compareTo(other: SemanticVersion): Int =
        when {
            major > other.major -> 1
            major < other.major -> -1
            minor > other.minor -> 1
            minor < other.minor -> -1
            patch > other.patch -> 1
            patch < other.patch -> -1
            parsedPreRelease != null && other.parsedPreRelease == null -> -1
            parsedPreRelease == null && other.parsedPreRelease != null -> 1
            parsedPreRelease != null && other.parsedPreRelease != null ->
                parsedPreRelease.compareTo(other.parsedPreRelease)
            else -> 0
        }

    public override fun equals(other: Any?): Boolean {
        val version = other as? SemanticVersion
        return when {
            version == null -> false
            compareTo(version) == 0 -> true
            else -> false
        }
    }

    public override fun hashCode(): Int {
        var hash = major.hashCode()
        hash *= 31 + minor.hashCode()
        hash *= 31 + patch.hashCode()
        hash *= parsedPreRelease?.let { 31 + parsedPreRelease.hashCode() } ?: 1
        return hash
    }

    public override fun toString(): String =
        "$major.$minor.$patch${parsedPreRelease?.let { "-$parsedPreRelease" } ?: ""}" +
            (buildMetadata?.let { "+$buildMetadata" } ?: "")

    /** Component function that returns the MAJOR number of the version upon destructuring. */
    public operator fun component1(): Int = major
    /** Component function that returns the MINOR number of the version upon destructuring. */
    public operator fun component2(): Int = minor
    /** Component function that returns the PATCH number of the version upon destructuring. */
    public operator fun component3(): Int = patch
    /** Component function that returns the PRE-RELEASE identifier of the version upon destructuring. */
    public operator fun component4(): String? = preRelease
    /** Component function that returns the BUILD metadata of the version upon destructuring. */
    public operator fun component5(): String? = buildMetadata

    /** Companion object of [SemanticVersion]. */
    public companion object {
        private val versionRegex: Regex = Patterns.VERSION_REGEX.toRegex()
        private val looseVersionRegex: Regex = Patterns.LOOSE_VERSION_REGEX.toRegex()

        /**
         * The 0.0.0 semantic version.
         *
         * @sample com.osmerion.kotlin.semver.samples.VersionSamples.min
         */
        public val min: SemanticVersion = SemanticVersion()

        /**
         * Parses the [versionString] as a [SemanticVersion] and returns the result or throws a [VersionFormatException]
         * if the string is not a valid representation of a semantic version.
         *
         * Strict mode is on by default, which means partial versions (e.g. '1.0' or '1') and versions with 'v' prefix
         * are considered invalid. This behaviour can be turned off by setting [strict] to false.
         *
         * @sample com.osmerion.kotlin.semver.samples.VersionSamples.parseStrict
         * @sample com.osmerion.kotlin.semver.samples.VersionSamples.parseLoose
         */
        @JvmOverloads
        @JvmStatic
        @Suppress("MagicNumber")
        public fun parse(versionString: String, strict: Boolean = true): SemanticVersion {
            val regex = if (strict) versionRegex else looseVersionRegex
            val result = regex.matchEntire(versionString)
                ?: throw VersionFormatException("Invalid version: $versionString")
            val major = result.groupValues[1].toIntOrNull()
            val minor = result.groupValues[2].toIntOrNull()
            val patch = result.groupValues[3].toIntOrNull()
            val preRelease = result.groups[4]?.value
            val buildMetadata = result.groups[5]?.value

            return when {
                strict && major != null && minor != null && patch != null ->
                    SemanticVersion(major, minor, patch, preRelease, buildMetadata)
                !strict && major != null ->
                    SemanticVersion(major, minor ?: 0, patch ?: 0, preRelease, buildMetadata)
                else -> throw VersionFormatException("Invalid version: $versionString")
            }
        }

        /**
         * Parses the string as a [SemanticVersion] and returns the result or null
         * if the string is not a valid representation of a semantic version.
         *
         * Strict mode is on by default, which means partial versions (e.g. '1.0' or '1') and versions with 'v' prefix are
         * considered invalid. This behaviour can be turned off by setting [strict] to false.
         *
         * @sample com.osmerion.kotlin.semver.samples.VersionSamples.toVersionOrNullStrict
         * @sample com.osmerion.kotlin.semver.samples.VersionSamples.toVersionOrNullLoose
         */
        public fun tryParse(versionString: String, strict: Boolean = true): SemanticVersion? =
            try {
                parse(versionString, strict)
            } catch (_: Exception) {
                null
            }

        // used by extensions only
        internal operator fun invoke(
            major: Int,
            minor: Int,
            patch: Int,
            preRelease: PreRelease?,
            buildMetadata: String? = null
        ): SemanticVersion = SemanticVersion(major, minor, patch, preRelease, buildMetadata)
    }

    /**
     * Increments the version by its MAJOR number. When the [preRelease] parameter is set, a pre-release version
     * will be produced from the next MAJOR version. The value of [preRelease] will be the first
     * pre-release identifier of the new version.
     *
     * Returns a new version while the original remains unchanged.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.nextMajor
     */
    public fun nextMajor(preRelease: String? = null): SemanticVersion = SemanticVersion(
        major + 1,
        0,
        0,
        preRelease?.let { PreRelease(preRelease) }
    )

    /**
     * Increments the version by its MINOR number. When the [preRelease] parameter is set, a pre-release version
     * will be produced from the next MINOR version. The value of [preRelease] will be the first
     * pre-release identifier of the new version.
     *
     * Returns a new version while the original remains unchanged.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.nextMinor
     */
    public fun nextMinor(preRelease: String? = null): SemanticVersion = SemanticVersion(
        major,
        minor + 1,
        0,
        preRelease?.let { PreRelease(preRelease) }
    )

    /**
     * Increments the version by its PATCH number. When the version is pre-release, the PATCH number will not be
     * incremented, only the pre-release identifier will be removed.
     *
     * When the [preRelease] parameter is set, a pre-release version will be produced from the next PATCH version.
     * The value of [preRelease] will be the first pre-release identifier of the new version.
     *
     * Returns a new version while the original remains unchanged.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.nextPatch
     */
    public fun nextPatch(preRelease: String? = null): SemanticVersion = SemanticVersion(
        major,
        minor,
        if (parsedPreRelease == null || preRelease != null) patch + 1 else patch,
        preRelease?.let { PreRelease(preRelease) }
    )

    /**
     * Increments the version by its PRE-RELEASE identifier or produces the next pre-release of a stable version.
     * The [preRelease] parameter's value is used for setting the pre-release identity when the version is stable or has
     * a different pre-release name. If the version is already pre-release and the first identifier matches with
     * the [preRelease] parameter, a simple incrementation will apply.
     *
     * Returns a new version while the original remains unchanged.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.nextPreRelease
     */
    public fun nextPreRelease(preRelease: String? = null): SemanticVersion = SemanticVersion(
        major,
        minor,
        parsedPreRelease?.let { patch } ?: (patch + 1),
        preRelease?.let {
            if (parsedPreRelease?.identity == it) parsedPreRelease.increment() else PreRelease(
                preRelease
            )
        } ?: parsedPreRelease?.increment() ?: PreRelease.default
    )

    /**
     * Increases the version [by] its [Inc.MAJOR], [Inc.MINOR], [Inc.PATCH], or [Inc.PRE_RELEASE] segment.
     *
     * [Inc.MAJOR] -> [nextMajor]
     *
     * [Inc.MINOR] -> [nextMinor]
     *
     * [Inc.PATCH] -> [nextPatch]
     *
     * [Inc.PRE_RELEASE] -> [nextPreRelease]
     *
     * Returns a new version while the original remains unchanged.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.inc
     */
    public fun inc(by: Inc, preRelease: String? = null): SemanticVersion =
        when (by) {
            Inc.MAJOR -> nextMajor(preRelease)
            Inc.MINOR -> nextMinor(preRelease)
            Inc.PATCH -> nextPatch(preRelease)
            Inc.PRE_RELEASE -> nextPreRelease(preRelease)
        }

    /**
     * Produces a copy of the [SemanticVersion] without the PRE-RELEASE and BUILD METADATA identities.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.withoutSuffixes
     */
    public fun withoutSuffixes(): SemanticVersion = this.copy(preRelease = null, buildMetadata = null)

    /**
     * Determines whether a [SemanticVersion] satisfies a [SemanticVersionConstraint] or not.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.satisfies
     */
    public infix fun satisfies(constraint: SemanticVersionConstraint): Boolean = constraint satisfiedBy this

    /**
     * Determines whether a [SemanticVersion] satisfies each [SemanticVersionConstraint] in a collection or not.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.satisfiesAll
     */
    public infix fun satisfiesAll(constraints: Iterable<SemanticVersionConstraint>): Boolean =
        constraints.all { constraint -> constraint satisfiedBy this }

    /**
     * Determines whether a [SemanticVersion] satisfies at least one [SemanticVersionConstraint] in a collection or not.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.satisfiesAny
     */
    public infix fun satisfiesAny(constraints: Iterable<SemanticVersionConstraint>): Boolean =
        constraints.any { constraint -> constraint satisfiedBy this }

}
