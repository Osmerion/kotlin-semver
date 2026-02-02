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

import com.osmerion.kotlin.semver.constraints.ExperimentalConstraintApi
import com.osmerion.kotlin.semver.constraints.npm.NpmConstraintFormat
import com.osmerion.kotlin.semver.internal.VersionRange
import com.osmerion.kotlin.semver.constraints.VersionPredicate
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
@OptIn(ExperimentalConstraintApi::class)
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
        public fun parse(source: CharSequence, format: ConstraintFormat = NpmConstraintFormat): VersionConstraint {
            val (comparators, preferredVersion) = format.parse(source)
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
        public fun tryParse(source: CharSequence, format: ConstraintFormat = NpmConstraintFormat): VersionConstraint? = try {
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

    override fun toString(): String = format.toString(predicates)

    /**
     * Determines whether this constraint is satisfied by the given [version].
     *
     * This method is shorthand for `isSatisfiedBy(version, includePreRelease = false)`.
     *
     * @param version           the [version][SemanticVersion] to check
     *
     * @return  whether the given constraint is satisfied by this version
     *
     * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.isSatisfiedBy
     *
     * @since   0.1.0
     */
    public infix fun isSatisfiedBy(version: SemanticVersion): Boolean =
        isSatisfiedBy(version, includePreRelease = false)

    /**
     * Determines whether this constraint is satisfied by the given [version].
     *
     * To check if a given version satisfies a constraint, the constraint is interpreted as a set of disjoint version
     * ranges. The constraint is satisfied by a version, iff the version lies within one these constructed ranges.
     *
     * Unless, [includePreRelease] is set, a pre-release version may not satisfy a constraint despite being technically
     * lying inside a range of versions for which this constraint is satisfied. Instead, pre-release versions usually
     * only satisfy a constraint, if other pre-release versions with the same normal version tuples contributed directly
     * to the constraint.
     *
     * For example, in [NPM][ConstraintFormat.NPM] constraint syntax, consider the constraint `>1.0.0-2`. Despite being
     * semantically greater than `1.0.0-2`, `1.1.0-0` does not satisfy the constraint while `1.0.0-3` does.
     *
     * @param version           the [version][SemanticVersion] to check
     * @param includePreRelease whether pre-release versions may satisfy the constraint
     *
     * @return  whether the given constraint is satisfied by this version
     *
     * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.isSatisfiedBy
     *
     * @since   0.1.0
     */
    public fun isSatisfiedBy(version: SemanticVersion, includePreRelease: Boolean): Boolean {
        var low = 0
        var high = ranges.lastIndex

        while (low <= high) {
            val mid = (low + high).ushr(1)
            val midRange = ranges[mid]

            if ((midRange.startInclusive == null || midRange.startInclusive <= version)
                && (midRange.endExclusive == null || version < midRange.endExclusive)
            ) {
                /*
                 * Pre-release versions only satisfy a constraint, if their normal version number is equal to the normal
                 * version number of either bound.
                 *
                 * For example:
                 * - [1.0.0-0, 1.2.3-2) is satisfied by 1.0.0-1 and 1.2.3-1 but not by 1.1.1-2.
                 * - [1.0.0, 1.2.0-0) is not satisfied by any pre-release version because none smaller than the upper
                 *   bound exists. (This is important for the correctness of inclusive constraints like [1.0.0, 1.1.0]!)
                 */
                return !version.isPreRelease
                    || includePreRelease
                    || (midRange.startInclusive?.isPreRelease == true && midRange.startInclusive.toNormalVersion() == version.toNormalVersion())
                    || (midRange.endExclusive?.isPreRelease == true && midRange.endExclusive.toNormalVersion() == version.toNormalVersion())
            } else if (midRange.startInclusive != null && version < midRange.startInclusive) {
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