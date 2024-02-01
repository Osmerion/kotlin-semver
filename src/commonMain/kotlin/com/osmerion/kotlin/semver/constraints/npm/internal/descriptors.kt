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
package com.osmerion.kotlin.semver.constraints.npm.internal

import com.osmerion.kotlin.semver.ConstraintFormatException
import com.osmerion.kotlin.semver.SemanticVersion
import com.osmerion.kotlin.semver.internal.PreRelease
import kotlin.jvm.JvmName

internal fun parseNpmVersionDescriptor(source: CharSequence): NpmVersionDescriptor {
    if (source == "*") return StarVersionDescriptor
    val result = SemVerPatterns.X_RANGE.matchEntire(source) ?: throw ConstraintFormatException("Invalid version descriptor: $source")

    val major = result.groupValues[3]
    val minor = result.groupValues[4].ifBlank { null }
    val patch = result.groupValues[5].ifBlank { null }
    val preRelease = result.groups[6]?.value?.let(::PreRelease)

    return RegularNpmVersionDescriptor(major, minor, patch, preRelease)
}

internal fun tryParseNpmVersionDescriptor(source: CharSequence): NpmVersionDescriptor? {
    return try {
        parseNpmVersionDescriptor(source)
    } catch (_: ConstraintFormatException) {
        null
    }
}

internal fun NpmVersionDescriptor.toVersion(): SemanticVersion? = when (this) {
    is RegularNpmVersionDescriptor -> SemanticVersion(major ?: 0, minor ?: 0, patch ?: 0, preRelease?.toString())
    is StarVersionDescriptor -> null
}

@JvmName("toVersionNullable")
internal fun NpmVersionDescriptor?.toVersion(): SemanticVersion? = this?.toVersion()

internal sealed interface NpmVersionDescriptor

internal data class RegularNpmVersionDescriptor(
    val majorString: String,
    val minorString: String? = null,
    val patchString: String? = null,
    val preRelease: PreRelease? = null
) : NpmVersionDescriptor {

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

    val major: Int? get() = majorString.toIntOrNull()
    val minor: Int? get() = minorString?.toIntOrNull()
    val patch: Int? get() = patchString?.toIntOrNull()

}

internal data object StarVersionDescriptor : NpmVersionDescriptor {
    override fun toString(): String = "*"
}
