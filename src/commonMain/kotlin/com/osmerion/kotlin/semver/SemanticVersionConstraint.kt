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

import com.osmerion.kotlin.semver.internal.constraints.HyphenConditionProcessor
import com.osmerion.kotlin.semver.internal.constraints.OperatorConditionProcessor
import com.osmerion.kotlin.semver.internal.constraints.VersionComparator
import com.osmerion.kotlin.semver.serializers.ConstraintSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmStatic

/**
 * This class describes a semantic version constraint. It provides ability to verify whether a version
 * [satisfies] one or more conditions within a constraint.
 *
 * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.constraint
 *
 * @since   0.1.0
 */
// TODO Refactor
@Serializable(with = ConstraintSerializer::class)
public class SemanticVersionConstraint private constructor(private val comparators: List<List<VersionComparator>>) {

    /**
     * Companion object of [SemanticVersionConstraint].
     *
     * @since   0.1.0
     */
    public companion object {

        private val default: SemanticVersionConstraint = SemanticVersionConstraint(listOf(listOf(VersionComparator.greaterThanMin)))
        private val conditionProcessors = arrayOf(
            HyphenConditionProcessor(),
            OperatorConditionProcessor()
        )

        /**
         * Parses the [constraintString] as a [SemanticVersionConstraint] and returns the result or throws
         * a [ConstraintFormatException] if the string is not a valid representation of a constraint.
         *
         * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.parse
         */
        // TODO Refactor
        @JvmStatic
        public fun parse(constraintString: String): SemanticVersionConstraint {
            if (constraintString.isBlank()) return default

            val orParts = constraintString.split("|").filter { part -> part.isNotBlank() }
            val comparators = orParts.map { comparator ->
                val conditionsResult = mutableListOf<VersionComparator>()
                var processed = comparator
                conditionProcessors.forEach { processor ->
                    processed = processed.replace(processor.regex) { condition ->
                        conditionsResult.add(processor.processCondition(condition))
                        ""
                    }
                }
                when {
                    processed.isNotBlank() -> throw ConstraintFormatException("Invalid constraint: $comparator")
                    else -> conditionsResult
                }
            }
            return when {
                comparators.isEmpty() || comparators.all { it.isEmpty() } -> throw ConstraintFormatException("Invalid constraint: $constraintString")
                else -> SemanticVersionConstraint(comparators)
            }
        }

        /**
         * Parses the given [string] argument as a [SemanticVersionConstraint]. If the string does not represent a
         * constraint, `null` is returned instead.
         *
         * @param string    the string to parse
         *
         * @return  the semantic version constraint represented by the argument, or `null`
         *
         * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.tryParse
         *
         * @since   0.1.0
         */
        @JvmStatic
        public fun tryParse(string: String): SemanticVersionConstraint? = try {
            parse(string)
        } catch (_: ConstraintFormatException) {
            null
        }

    }

    // TODO Refactor
    override fun toString(): String = comparators.joinToString(" || ") { it.joinToString(" ") }

    // TODO Refactor
    public override fun equals(other: Any?): Boolean =
        when (val constraint = other as? SemanticVersionConstraint) {
            null -> false
            else -> toString() == constraint.toString()
        }

    // TODO Refactor
    public override fun hashCode(): Int = toString().hashCode()

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
    public infix fun isSatisfiedBy(version: SemanticVersion): Boolean =
        comparators.any { comparator -> comparator.all { condition -> condition.isSatisfiedBy(version) } }

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