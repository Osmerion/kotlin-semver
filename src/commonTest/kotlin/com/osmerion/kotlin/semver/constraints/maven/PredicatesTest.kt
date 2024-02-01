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
package com.osmerion.kotlin.semver.constraints.maven

import com.osmerion.kotlin.semver.SemanticVersion
import com.osmerion.kotlin.semver.constraints.ExperimentalConstraintApi
import com.osmerion.kotlin.semver.constraints.maven.internal.ExactVersionMatch
import com.osmerion.kotlin.semver.constraints.maven.internal.IntervalVersionRange
import com.osmerion.kotlin.semver.constraints.maven.internal.MinimumVersion
import com.osmerion.kotlin.semver.constraints.maven.internal.parseMavenVersionDescriptor
import com.osmerion.kotlin.semver.internal.VersionRange
import com.osmerion.kotlin.semver.internal.toVersionRanges
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalConstraintApi::class)
class PredicatesTest {

    @Test
    fun testExactVersionMatch() {
        assertEquals(
            listOf(VersionRange(SemanticVersion(1), SemanticVersion(major = 1, patch = 1, preRelease = "0"))),
            listOf(listOf(ExactVersionMatch(parseMavenVersionDescriptor("1")))).toVersionRanges()
        )
        assertEquals(
            listOf(VersionRange(SemanticVersion(1, 2), SemanticVersion(major = 1, minor = 2, patch = 1, preRelease = "0"))),
            listOf(listOf(ExactVersionMatch(parseMavenVersionDescriptor("1.2")))).toVersionRanges()
        )
        assertEquals(
            listOf(VersionRange(SemanticVersion(1, 2, 3), SemanticVersion(major = 1, minor = 2, patch = 4, preRelease = "0"))),
            listOf(listOf(ExactVersionMatch(parseMavenVersionDescriptor("1.2.3")))).toVersionRanges()
        )
        assertEquals(
            listOf(VersionRange(SemanticVersion(1, 2, 3, "0"), SemanticVersion(major = 1, minor = 2, patch = 3, preRelease = "0.0"))),
            listOf(listOf(ExactVersionMatch(parseMavenVersionDescriptor("1.2.3-0")))).toVersionRanges()
        )
    }

    @Test
    fun testIntervalVersionRange() {
        assertEquals(
            listOf(VersionRange(SemanticVersion(1, 2, 3), SemanticVersion(2, 7, 5, "0"))),
            listOf(listOf(
                IntervalVersionRange(
                    lowerBound = parseMavenVersionDescriptor("1.2.3"),
                    lowerBoundInclusive = true,
                    upperBound = parseMavenVersionDescriptor("2.7.4"),
                    upperBoundInclusive = true
                )
            )).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(SemanticVersion(1, 2, 4, "0"), SemanticVersion(2, 7, 5, "0"))),
            listOf(listOf(
                IntervalVersionRange(
                    lowerBound = parseMavenVersionDescriptor("1.2.3"),
                    lowerBoundInclusive = false,
                    upperBound = parseMavenVersionDescriptor("2.7.4"),
                    upperBoundInclusive = true
                )
            )).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(SemanticVersion(1, 2, 3), SemanticVersion(2, 7, 4))),
            listOf(listOf(
                IntervalVersionRange(
                    lowerBound = parseMavenVersionDescriptor("1.2.3"),
                    lowerBoundInclusive = true,
                    upperBound = parseMavenVersionDescriptor("2.7.4"),
                    upperBoundInclusive = false
                )
            )).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(SemanticVersion(1, 2, 4, "0"), SemanticVersion(2, 7, 4))),
            listOf(listOf(
                IntervalVersionRange(
                    lowerBound = parseMavenVersionDescriptor("1.2.3"),
                    lowerBoundInclusive = false,
                    upperBound = parseMavenVersionDescriptor("2.7.4"),
                    upperBoundInclusive = false
                )
            )).toVersionRanges()
        )
    }

    @Test
    fun testMinimumVersion() {
        assertEquals(
            listOf(VersionRange(SemanticVersion(1), null)),
            listOf(listOf(MinimumVersion(parseMavenVersionDescriptor("1")))).toVersionRanges()
        )

    }

}
