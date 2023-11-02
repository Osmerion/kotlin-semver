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
package com.osmerion.kotlin.semver.constraints

import com.osmerion.kotlin.semver.SemanticVersion

/**
 * Determines whether a [Constraint] is satisfied by a [SemanticVersion] or not.
 *
 * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.satisfiedBy
 */
public infix fun Constraint.satisfiedBy(version: SemanticVersion): Boolean = this.isSatisfiedBy(version)

/**
 * Determines whether a [Constraint] is satisfied by each [SemanticVersion] in a collection or not.
 *
 * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.satisfiedByAll
 */
public infix fun Constraint.satisfiedByAll(versions: Iterable<SemanticVersion>): Boolean =
    versions.all { version -> this.isSatisfiedBy(version) }

/**
 * Determines whether a [Constraint] is satisfied by at least one [SemanticVersion] in a collection or not.
 *
 * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.satisfiedByAny
 */
public infix fun Constraint.satisfiedByAny(versions: Iterable<SemanticVersion>): Boolean =
    versions.any { version -> this.isSatisfiedBy(version) }

/**
 * Parses the string as a [Constraint] and returns the result or throws a [ConstraintFormatException]
 * if the string is not a valid representation of a constraint.
 *
 * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.toConstraint
 */
public fun String.toConstraint(): Constraint = Constraint.parse(this)

/**
 * Parses the string as a [Constraint] and returns the result or null
 * if the string is not a valid representation of a constraint.
 *
 * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.toConstraintOrNull
 */
public fun String.toConstraintOrNull(): Constraint? = try { this.toConstraint() } catch (_: Exception) { null }
