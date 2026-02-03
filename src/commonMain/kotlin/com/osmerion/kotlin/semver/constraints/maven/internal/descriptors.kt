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
package com.osmerion.kotlin.semver.constraints.maven.internal

import com.osmerion.kotlin.semver.ConstraintFormatException
import com.osmerion.kotlin.semver.Version
import com.osmerion.kotlin.semver.internal.Patterns
import com.osmerion.kotlin.semver.internal.PreRelease
import kotlin.jvm.JvmName

private val MAVEN_VERSION_DESCRIPTOR_REGEX by lazy { "(${Patterns.NUMERIC})(?:\\.(${Patterns.NUMERIC})(?:\\.(${Patterns.NUMERIC})${Patterns.PRE_RELEASE}?)?)?".toRegex() }

internal fun parseMavenVersionDescriptor(source: CharSequence): MavenVersionDescriptor {
    val result = MAVEN_VERSION_DESCRIPTOR_REGEX.matchEntire(source) ?: throw ConstraintFormatException("Invalid version descriptor: $source")
    val major = result.groupValues[1]
    val minor = result.groupValues[2].ifBlank { null }
    val patch = result.groupValues[3].ifBlank { null }
    val preRelease = result.groups[4]?.value?.let(::PreRelease)

    return MavenVersionDescriptor(major, minor, patch, preRelease)
}

internal fun MavenVersionDescriptor.toVersion(increment: Boolean = false): Version {
    return Version(major, minor ?: 0, patch ?: 0, preRelease?.toString())
        .let { if (increment) it.toSmallestLargerVersion() else it }
}

@JvmName("toVersionNullable")
internal fun MavenVersionDescriptor?.toVersion(increment: Boolean = false): Version? = this?.toVersion(increment)

internal data class MavenVersionDescriptor(
    private val majorString: String,
    private val minorString: String? = null,
    private val patchString: String? = null,
    val preRelease: PreRelease? = null
) {

    override fun toString(): String = buildString {
        append(majorString)
        if (minorString != null) {
            append(".$minorString")

            if (patchString != null) {
                append(".$patchString")
                if (preRelease != null) append("-$preRelease")
            }
        }
    }

    val major: Int get() = majorString.toInt()
    val minor: Int? get() = minorString?.toIntOrNull()
    val patch: Int? get() = patchString?.toIntOrNull()

}
