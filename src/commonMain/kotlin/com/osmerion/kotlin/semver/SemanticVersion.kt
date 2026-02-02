/*
 * Copyright (c) 2022 Peter Csajtai
 * Copyright (c) 2023-2026 Leon Linhart
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
import com.osmerion.kotlin.semver.internal.PreRelease
import com.osmerion.kotlin.semver.serializers.VersionSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * A semantic version according to the [Semantic Versioning 2.0.0 specification](https://semver.org/spec/v2.0.0.html).
 *
 * The natural ordering of this class is inconsistent with [equals] since [build metadata][buildMetadata] does not
 * factor into semantic version precedence. For more information refer to [compareTo].
 *
 * Instances of this class are immutable and can safely be used across multiple threads.
 *
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.explode
 *
 * @since   0.1.0
 */
@Serializable(with = VersionSerializer::class)
public class SemanticVersion private constructor(
    /**
     * The `MAJOR` number of the version.
     *
     * The major version must be incremented whenever an API incompatible change is made. See [toNextMajor] for more
     * information on incrementing major version numbers.
     *
     * @since   0.1.0
     */
    public val major: Int,
    /**
     * The `MINOR` number of the version.
     *
     * The minor version must be incremented whenever functionality is added in a backward compatible manner. See
     * [toNextMinor] for more information on incrementing minor version numbers.
     *
     * @since   0.1.0
     */
    public val minor: Int,
    /**
     * The `PATCH` number of the version.
     *
     * The patch version must be incremented whenever backward compatible bug fixes are made. See [toNextPatch] for more
     * information on incrementing patch version numbers.
     *
     * @since   0.1.0
     */
    public val patch: Int,
    internal val parsedPreRelease: PreRelease? = null,
    /**
     * The build metadata of the version.
     *
     * @since   0.1.0
     */
    public val buildMetadata: String? = null
) : Comparable<SemanticVersion> {

    /**
     * Constructs a semantic version from the given arguments following the pattern:
     * <[major]>.<[minor]>.<[patch]>-<[preRelease]>+<[buildMetadata]>
     *
     * @param major         the `MAJOR` version number
     * @param minor         the `MINOR` version number
     * @param patch         the `PATCH` version number
     * @param preRelease    the pre-release identifier
     * @param buildMetadata the build metadata
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.construct
     *
     * @since   0.1.0
     */
    public constructor(
        major: Int = 0,
        minor: Int = 0,
        patch: Int = 0,
        preRelease: String? = null,
        buildMetadata: String? = null
    ) : this(major, minor, patch, preRelease?.let(::PreRelease), buildMetadata)

    init {
        when {
            major < 0 -> throw VersionFormatException("The major number must be >= 0.")
            minor < 0 -> throw VersionFormatException("The minor number must be >= 0.")
            patch < 0 -> throw VersionFormatException("The patch number must be >= 0.")
        }
    }

    /**
     * Companion object of [SemanticVersion].
     *
     * @since   0.1.0
     */
    public companion object {

        private val versionRegex: Regex by lazy { Patterns.VERSION_REGEX.toRegex() }
        private val looseVersionRegex: Regex by lazy { Patterns.PREFIXED_LOOSE_VERSION_REGEX.toRegex() }

        /**
         * Parses the given [source] argument as a [SemanticVersion]. If the string does not represent a semantic
         * version, a [VersionFormatException] is thrown.
         *
         * By default, this function only accepts version strings that conform to the Semantic Versioning Specification
         * 2.0.0. Thus, partial versions (e.g. `1.0` or `1`) and versions with `v` prefix are considered invalid. This
         * _strict_ behavior can be turned off by setting [strict] to false.
         *
         * @param source    the source to parse
         * @param strict    whether to strictly match spec-compliant version strings only
         *
         * @return  the semantic version represented by the argument
         *
         * @sample com.osmerion.kotlin.semver.samples.VersionSamples.parseStrict
         * @sample com.osmerion.kotlin.semver.samples.VersionSamples.parseLoose
         *
         * @since   0.1.0
         */
        @JvmOverloads
        @JvmStatic
        @Suppress("MagicNumber")
        public fun parse(source: CharSequence, strict: Boolean = true): SemanticVersion {
            val regex = if (strict) versionRegex else looseVersionRegex
            val result = regex.matchEntire(source) ?: throw VersionFormatException("Invalid version string: $source")
            val major = result.groupValues[1].toIntOrNull()
            val minor = result.groupValues[2].toIntOrNull()
            val patch = result.groupValues[3].toIntOrNull()
            val preRelease = result.groups[4]?.value
            val buildMetadata = result.groups[5]?.value

            return when {
                strict && major != null && minor != null && patch != null -> SemanticVersion(major, minor, patch, preRelease, buildMetadata)
                !strict && major != null -> SemanticVersion(major, minor ?: 0, patch ?: 0, preRelease, buildMetadata)
                else -> throw VersionFormatException("Invalid version string: $source")
            }
        }

        /**
         * Parses the given [source] argument as a [SemanticVersion]. If the string does not represent a semantic
         * version, `null` is returned instead.
         *
         * By default, this function only accepts version strings that conform to the Semantic Versioning Specification
         * 2.0.0. Thus, partial versions (e.g. `1.0` or `1`) and versions with `v` prefix are considered invalid. This
         * _strict_ behavior can be turned off by setting [strict] to false.
         *
         * @param source    the source to parse
         * @param strict    whether to strictly match spec-compliant version strings only
         *
         * @return  the semantic version represented by the argument, or `null`
         *
         * @sample com.osmerion.kotlin.semver.samples.VersionSamples.tryParseStrict
         * @sample com.osmerion.kotlin.semver.samples.VersionSamples.tryParseLoose
         *
         * @since   0.1.0
         */
        @JvmOverloads
        @JvmStatic
        public fun tryParse(source: CharSequence, strict: Boolean = true): SemanticVersion? = try {
            parse(source, strict)
        } catch (_: VersionFormatException) {
            null
        }

    }

    /**
     * The pre-release identifier of the version.
     *
     * @since   0.1.0
     */
    public val preRelease: String?
        get() = parsedPreRelease?.toString()

    /**
     * Constructs a copy of this [SemanticVersion].
     *
     * The copied object's properties can be altered with the optional parameters.
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.copy
     *
     * @since   0.1.0
     */
    public fun copy(
        major: Int = this.major,
        minor: Int = this.minor,
        patch: Int = this.patch,
        preRelease: String? = this.preRelease,
        buildMetadata: String? = this.buildMetadata
    ): SemanticVersion =
        SemanticVersion(major, minor, patch, preRelease, buildMetadata)

    /**
     * Compares this version with the given [other] version for order.
     *
     * The ordering of [semantic versions][SemanticVersion] is consistent with the definition from the Semantic
     * Versioning 2.0.0 specification:
     *
     *  1. Precedence is determined by the first difference when comparing the major, minor, patch and pre-release
     *     identifiers in this order.
     *
     *  2. A pre-release version has a lower precedence than a normal version.
     *
     *  3. Precedence of two pre-release versions is determined by comparing each dot-separated part of the pre-release
     *     identifier from left to right as follows:
     *
     *      1. Parts consisting of only digits are compared numerically.
     *      2. Parts with letters or hyphens are compared lexically in ASCII sort order.
     *      3. Numeric parts always have lower precedence than non-numeric parts.
     *      4. A larger set of pre-release parts has a higher precedence than a smaller set, if all the preceding parts
     *         are equal.
     *
     * The build metadata of a version is ignored when determining its relative order. Since the build metadata is
     * included in equality checks, the natural ordering of [SemanticVersion] is inconsistent with [equals].
     * Specifically, `e1.compareTo(e2) == 0` does not imply `e1.equals(e2)` exactly if `e1.buildMetadata != e2.buildMetadata`.
     *
     * @param other the version to be compared
     *
     * @return  a negative integer, zero, or a positive integer as this version is less than, equal to, or greater than
     *          the given other version
     *
     * @since   0.1.0
     */
    public override fun compareTo(other: SemanticVersion): Int = when {
        major > other.major -> 1
        major < other.major -> -1
        minor > other.minor -> 1
        minor < other.minor -> -1
        patch > other.patch -> 1
        patch < other.patch -> -1
        parsedPreRelease != null && other.parsedPreRelease == null -> -1
        parsedPreRelease == null && other.parsedPreRelease != null -> 1
        parsedPreRelease != null && other.parsedPreRelease != null -> parsedPreRelease.compareTo(other.parsedPreRelease)
        else -> 0
    }

    public override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other is SemanticVersion -> major == other.major
            && minor == other.minor
            && patch == other.patch
            && parsedPreRelease == other.parsedPreRelease
            && buildMetadata == other.buildMetadata
        else -> false
    }

    public override fun hashCode(): Int {
        /*
         * Implementation Note:
         * We do not let build metadata factor into the hashcode calculation. Thus, it is possible that
         * `e1.hashCode() == e2.hashCode()` while `e1 != e2`. This is in line with the specification though, since we
         * are only required to return the same hashcode for equal objects. (Same hashcode DOES NOT imply object
         * equality.)
         *
         * The advantage of this approach is that we have a better locality for any hash-based collection that works
         * with semantic versions. This is useful, since build metadata does not factor into version precedence.
         */
        var hash = major.hashCode()
        hash *= 31 + minor.hashCode()
        hash *= 31 + patch.hashCode()
        hash *= parsedPreRelease?.let { 31 + parsedPreRelease.hashCode() } ?: 1
        return hash
    }

    /**
     * Returns the string representation of this version.
     *
     * The returned string has the format: <[major]>.<[minor]>.<[patch]>(-<[preRelease]>?)(+<[buildMetadata]>?)
     *
     * @return  the string representation of this version
     *
     * @since   0.1.0
     */
    public override fun toString(): String =
        "$major.$minor.$patch${parsedPreRelease?.let { "-$parsedPreRelease" } ?: ""}${(buildMetadata?.let { "+$buildMetadata" } ?: "")}"

    /**
     * Component function that returns the [major] number of this version upon destructuring.
     *
     * @return  the major number of this version
     *
     * @since   0.1.0
     */
    public operator fun component1(): Int = major

    /**
     * Component function that returns the [minor] number of this version upon destructuring.
     *
     * @return  the minor number of this version
     *
     * @since   0.1.0
     */
    public operator fun component2(): Int = minor

    /**
     * Component function that returns the [patch] number of this version upon destructuring.
     *
     * @return  the patch number of this version
     *
     * @since   0.1.0
     */
    public operator fun component3(): Int = patch

    /**
     * Component function that returns the [pre-release][preRelease] identifier of this version upon destructuring.
     *
     * @return  the pre-release identifier of this version, or `null`
     *
     * @since   0.1.0
     */
    public operator fun component4(): String? = preRelease

    /**
     * Component function that returns the [build metadata][buildMetadata] of this version upon destructuring.
     *
     * @return  the build metadata of this version, or `null`
     *
     * @since   0.1.0
     */
    public operator fun component5(): String? = buildMetadata

    /**
     * Returns whether this version represents a normal version number.
     *
     * A normal version number has the format <[major]>.<[minor]>.<[patch]> without any [pre-release][preRelease]
     * identifier or [build metadata][buildMetadata].
     *
     * @since   0.1.0
     */
    public val isNormal: Boolean
        get() = parsedPreRelease == null && buildMetadata == null

    /**
     * Returns whether this version is a pre-release version.
     *
     * A version is a pre-release version exactly when it has a [pre-release][preRelease] identifier.
     *
     * @since   0.1.0
     */
    public val isPreRelease: Boolean
        get () = parsedPreRelease != null

    /**
     * Returns whether this version is considered stable.
     *
     * A version is stable exactly when the [major] number is greater than zero  and when it has no [pre-release][preRelease]
     * identifier.
     *
     * @since   0.1.0
     */
    public val isStable: Boolean
        get() = major > 0 && parsedPreRelease == null

    /**
     * Produces a copy of this version incremented to the next major version.
     *
     * In accordance with the Semantic Versioning Specification 2.0.0, the minor and patch version numbers are set to
     * zero.
     *
     * If [preRelease] is not `null`, it is included in the returned version and, hence, the parameter may be used to
     * create a pre-release version instead of a normal version.
     *
     * @return  a new version that represents the result of incrementing the major version number of this version
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.toNextMajor
     *
     * @since   0.1.0
     */
    @JvmOverloads
    public fun toNextMajor(preRelease: String? = null): SemanticVersion = SemanticVersion(
        major = major + 1,
        minor = 0,
        patch = 0,
        parsedPreRelease = preRelease?.let(::PreRelease)
    )

    /**
     * Produces a copy of this version incremented to the next minor version.
     *
     * In accordance with the Semantic Versioning Specification 2.0.0, the major version number is left untouched and
     * the patch version number is set to zero.
     *
     * If [preRelease] is not `null`, it is included in the returned version and, hence, the parameter may be used to
     * create a pre-release version instead of a normal version.
     *
     * @return  a new version that represents the result of incrementing the minor version number of this version
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.toNextMinor
     *
     * @since   0.1.0
     */
    @JvmOverloads
    public fun toNextMinor(preRelease: String? = null): SemanticVersion = SemanticVersion(
        major = major,
        minor = minor + 1,
        patch = 0,
        parsedPreRelease = preRelease?.let(::PreRelease)
    )

    /**
     * Produces a copy of this version incremented to the next patch version.
     *
     * In accordance with the Semantic Versioning Specification 2.0.0, the major and minor version numbers are left
     * untouched.
     *
     * If this [SemanticVersion] represents a pre-release version and [preRelease] is `null`, the patch version number
     * is not incremented. Instead, the pre-release identifier of this version is simply dropped to produce the next
     * patch version.
     *
     * If [preRelease] is not `null`, it is included in the returned version and, hence, the parameter may be used to
     * create a pre-release version instead of a normal version.
     *
     * @return  the result of incrementing this version to the next patch version
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.toNextPatch
     *
     * @since   0.1.0
     */
    @JvmOverloads
    public fun toNextPatch(preRelease: String? = null): SemanticVersion = SemanticVersion(
        major = major,
        minor = minor,
        patch = if (parsedPreRelease == null || preRelease != null) patch + 1 else patch,
        parsedPreRelease = preRelease?.let(::PreRelease)
    )

    /**
     * Produces a copy of this version without [pre-release][preRelease] identifier.
     *
     * @return  a copy of this version without pre-release identifier
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.removePreRelease
     *
     * @since   0.1.0
     */
    public fun removePreRelease(): SemanticVersion =
        copy(preRelease = null)

    /**
     * Produces a copy of this version without [build metadata][buildMetadata].
     *
     * @return  a copy of this version without build metadata
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.removeBuildMetadata
     *
     * @since   0.1.0
     */
    public fun removeBuildMetadata(): SemanticVersion =
        copy(buildMetadata = null)

    /**
     * Produces a copy of this version without [pre-release][preRelease] identifier and [build metadata][buildMetadata].
     *
     * @return  a copy of this version without pre-release identifier and build metadata
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.removePreRelease
     *
     * @since   0.1.0
     */
    public fun toNormalVersion(): SemanticVersion =
        copy(preRelease = null, buildMetadata = null)

    /**
     * Determines whether this version satisfies the given [constraint].
     *
     * @param constraint    the [constraint][VersionConstraint] to check
     *
     * @return  whether the given constraint is satisfied by this version
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.satisfies
     *
     * @since   0.1.0
     */
    public infix fun satisfies(constraint: VersionConstraint): Boolean =
        constraint isSatisfiedBy this

    /**
     * Determines whether this version satisfies all the given [constraints].
     *
     * @param constraints   the  [constraints][VersionConstraint] to check
     *
     * @return  whether all given constraints are satisfied by this version
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.satisfiesAll
     *
     * @since   0.1.0
     */
    public infix fun satisfiesAll(constraints: Iterable<VersionConstraint>): Boolean =
        constraints.all { constraint -> constraint isSatisfiedBy this }

    /**
     * Determines whether this version satisfies any of the given [constraints].
     *
     * @param constraints   the  [constraints][VersionConstraint] to check
     *
     * @return  whether any of the given constraints is satisfied by this version
     *
     * @sample com.osmerion.kotlin.semver.samples.VersionSamples.satisfiesAny
     *
     * @since   0.1.0
     */
    public infix fun satisfiesAny(constraints: Iterable<VersionConstraint>): Boolean =
        constraints.any { constraint -> constraint isSatisfiedBy this }

    @JvmSynthetic
    internal fun toSmallestLargerVersion(): SemanticVersion {
        var patch = patch
        val preRelease = if (parsedPreRelease != null) {
            parsedPreRelease.toSmallestLargerVersion()
        } else {
            patch += 1
            PreRelease.MIN
        }

        return SemanticVersion(major, minor, patch, preRelease)
    }

}
