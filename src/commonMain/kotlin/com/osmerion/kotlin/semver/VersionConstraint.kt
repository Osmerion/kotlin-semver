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

import com.osmerion.kotlin.semver.internal.VersionRange
import com.osmerion.kotlin.semver.internal.constraints.VersionPredicate
import com.osmerion.kotlin.semver.internal.constraints.formats.maven.parseMavenConstraint
import com.osmerion.kotlin.semver.internal.constraints.formats.npm.parseNpmConstraint
import com.osmerion.kotlin.semver.internal.constraints.formats.osmerion.parseOsmerionConstraint
import com.osmerion.kotlin.semver.internal.toVersionRanges
import com.osmerion.kotlin.semver.serializers.DefaultConstraintSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * A version constraint for [semantic versions][SemanticVersion].
 *
 * A version constraint is a combination of predicates that a version can be checked against. For more information refer
 * to [isSatisfiedBy].
 *
 * When using a constraint to resolve a version, usually the [largest][SemanticVersion.compareTo] version that satisfies
 * the constraint should be picked. However, some constraints may also specify a [preferred version][preferredVersion].
 * Thus, if a constraint has a preferred version, this version should be picked instead, given that it satisfies all
 * applicable constraints. For example, consider the scenario:
 *
 * - `A` is a library with the available versions: `1.0.0, 1.1.0, 1.2.0, and 1.3.0`
 * - `B` is library that depends on `A` while constraining the version of A to at least 1.1,
 *
 * If an application depends on A with a constraint that prefers version 1.0 but is also satisfied by larger versions,
 * and on library B, the preferred version cannot be picked as it does not satisfy the constraints implied by the
 * dependency on B. Instead, the resolution should fall back to picking the largest version.
 *
 * Instances of this class are immutable and can safely be used across multiple threads.
 *
 * @param preferredVersion  the preferred version of the constraint
 *
 * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.constraint
 *
 * @since   0.1.0
 */
@OptIn(ExperimentalConstraintFormat::class)
@Serializable(with = DefaultConstraintSerializer::class)
public class VersionConstraint private constructor(
    private val predicates: List<List<VersionPredicate>>,
    private val format: ConstraintFormat,
    public val preferredVersion: SemanticVersion?
) {

    /**
     * Companion object of [VersionConstraint].
     *
     * @since   0.1.0
     */
    public companion object {

        /**
         * Parses the [source] as a [VersionConstraint] and returns the result or throws a  [ConstraintFormatException]
         * if the string is not a valid representation of a constraint in the given [format].
         *
         * @param source    the source to parse
         * @param format    the format of the constraint string
         *
         * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.parse
         *
         * @since   0.1.0
         */
        @JvmOverloads
        @JvmStatic
        public fun parse(source: CharSequence, format: ConstraintFormat = ConstraintFormat.NPM): VersionConstraint {
            if (source.isBlank()) throw ConstraintFormatException("Constraint strings may not be blank")

            val (comparators, preferredVersion) = when (format) {
                ConstraintFormat.MAVEN -> parseMavenConstraint(source)
                ConstraintFormat.NPM -> parseNpmConstraint(source)
                ConstraintFormat.OSMERION -> parseOsmerionConstraint(source)
            }

            return when {
                comparators.isEmpty() || comparators.all(List<VersionPredicate>::isEmpty) -> throw ConstraintFormatException("Invalid constraint: $source")
                else -> VersionConstraint(comparators, format, preferredVersion)
            }
        }

        /**
         * Parses the given [source] argument as a [VersionConstraint]. If the string does not represent a constraint in
         * the given [format], `null` is returned instead.
         *
         * @param source    the source to parse
         * @param format    the format of the constraint string
         *
         * @return  the semantic version constraint represented by the argument, or `null`
         *
         * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.tryParse
         *
         * @since   0.1.0
         */
        @JvmOverloads
        @JvmStatic
        public fun tryParse(source: CharSequence, format: ConstraintFormat = ConstraintFormat.NPM): VersionConstraint? = try {
            parse(source, format)
        } catch (_: ConstraintFormatException) {
            null
        }

    }

    private val ranges: List<VersionRange> = this.predicates.toVersionRanges()

    public override fun equals(other: Any?): Boolean = when {
        this === other -> true
        // The order of comparators does not matter for constraint equality
        other is VersionConstraint -> ranges == other.ranges
            && preferredVersion == other.preferredVersion
        else -> false
    }

    // The order of comparators does not matter for constraint equality
    public override fun hashCode(): Int {
        var hash = ranges.hashCode()
        hash *= 31 + preferredVersion.hashCode()
        return hash
    }

    override fun toString(): String = when (format) {
        ConstraintFormat.OSMERION -> predicates.joinToString(separator = "||") { "${it.single()}" }
        ConstraintFormat.MAVEN -> predicates.joinToString(separator = ",") { "${it.single()}" }
        ConstraintFormat.NPM -> predicates.joinToString(separator = "||") { it.joinToString(separator = " ") }
    }

    /**
     * Determines whether this constraint is satisfied by the given [version].
     *
     * @param version   the [version][SemanticVersion] to check
     *
     * @return  whether the given constraint is satisfied by this version
     *
     * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.isSatisfiedBy
     *
     * @since   0.1.0
     */
    public infix fun isSatisfiedBy(version: SemanticVersion): Boolean {
        var low = 0
        var high = ranges.size

        while (low <= high) {
            val mid = (low + high).ushr(1)
            val midRange = ranges[mid]

            if ((midRange.startInclusive == null || midRange.startInclusive <= version)
                && (midRange.endExclusive == null || version < midRange.endExclusive)
            ) {
                return true
            } else if (midRange.startInclusive == null || version < midRange.startInclusive) {
                high = mid - 1
            } else {
                low = mid + 1
            }
        }

        return false
    }

    /**
     * Determines whether this constraint is satisfied by all the given [versions].
     *
     * @param versions  the [versions][SemanticVersion] to check
     *
     * @return  whether all given versions satisfy this constraint
     *
     * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.isSatisfiedByAll
     *
     * @since   0.1.0
     */
    public infix fun isSatisfiedByAll(versions: Iterable<SemanticVersion>): Boolean =
        versions.all { version -> this isSatisfiedBy version }

    /**
     * Determines whether this constraint is satisfied by any of the given [versions].
     *
     * @param versions  the [versions][SemanticVersion] to check
     *
     * @return  whether any of the given versions satisfies this constraint
     *
     * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.isSatisfiedByAny
     *
     * @since   0.1.0
     */
    public infix fun isSatisfiedByAny(versions: Iterable<SemanticVersion>): Boolean =
        versions.any { version -> this isSatisfiedBy version }

}