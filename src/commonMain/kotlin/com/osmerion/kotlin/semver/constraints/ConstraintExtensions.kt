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
