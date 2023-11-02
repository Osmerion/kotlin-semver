package com.osmerion.kotlin.semver.constraints

/**
 * [Constraint] throws this exception when the constraint parsing fails due to an invalid format.
 *
 * @sample com.osmerion.kotlin.semver.samples.ConstraintSamples.exception
 */
public class ConstraintFormatException(message: String) : Exception(message)
