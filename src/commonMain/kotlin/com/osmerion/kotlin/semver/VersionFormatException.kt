package com.osmerion.kotlin.semver

/**
 * [Version] throws this exception when the semantic version parsing fails due to an invalid format.
 *
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.exception
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.preReleaseException
 */
public class VersionFormatException(message: String) : Exception(message)
