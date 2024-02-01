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

import com.osmerion.kotlin.semver.ConstraintFormatException
import com.osmerion.kotlin.semver.VersionConstraint
import com.osmerion.kotlin.semver.constraints.maven.MavenConstraintFormat
import com.osmerion.kotlin.semver.constraints.maven.internal.parseMavenVersionDescriptor
import com.osmerion.kotlin.semver.internal.PreRelease
import com.osmerion.kotlin.semver.constraints.maven.internal.MavenVersionDescriptor
import kotlin.test.*

/** Unit tests for [Maven][MavenConstraintFormat] version constraints. */
class MavenConstraintTest {

    @Test
    fun testParse_InvalidConstraints() {
        assertFailsWith<ConstraintFormatException> { VersionConstraint.parse("[1.0,1.0)", format = MavenConstraintFormat) }
        assertFailsWith<ConstraintFormatException> { VersionConstraint.parse("[2,1]", format = MavenConstraintFormat) }
        assertFailsWith<ConstraintFormatException> { VersionConstraint.parse("[1.0,1.2],1.3", format = MavenConstraintFormat) }
    }

    @Test
    fun testTryParse_InvalidConstraints() {
        assertNull(VersionConstraint.tryParse("[1.0,1.0)", format = MavenConstraintFormat))
        assertNull(VersionConstraint.tryParse("[2,1]", format = MavenConstraintFormat))
        assertNull(VersionConstraint.tryParse("[1.0,1.2],1.3", format = MavenConstraintFormat))
    }

    @Test
    fun testToString() {
        fun assertToStringEquals(expected: String, source: String = expected) {
            assertEquals(expected, VersionConstraint.parse(source, format = MavenConstraintFormat).toString())
        }

        assertToStringEquals("1.0")
        assertToStringEquals("(,1.0]")
        assertToStringEquals("(,1.0)")
        assertToStringEquals("[1.0]")
        assertToStringEquals("[1.0,)")
        assertToStringEquals("(1.0,)")
        assertToStringEquals("(1.0,2.0)")
        assertToStringEquals("[1.0,2.0]")
        assertToStringEquals("(,1.0],[1.2,)")
        assertToStringEquals("(,1.1),(1.1,)")
    }

    @Test
    fun testParseDescriptor() {
        assertEquals(MavenVersionDescriptor("1"), parseMavenVersionDescriptor("1"))
        assertEquals(MavenVersionDescriptor("1", "2"), parseMavenVersionDescriptor("1.2"))
        assertEquals(MavenVersionDescriptor("1", "2", "3"), parseMavenVersionDescriptor("1.2.3"))
        assertEquals(MavenVersionDescriptor("1", "2", "3", PreRelease("alpha")), parseMavenVersionDescriptor("1.2.3-alpha"))
        assertEquals(MavenVersionDescriptor("1", "2", "3", PreRelease("alpha.0")), parseMavenVersionDescriptor("1.2.3-alpha.0"))
        assertEquals(MavenVersionDescriptor("1", "2", "3", PreRelease("alpha-0")), parseMavenVersionDescriptor("1.2.3-alpha-0"))

        assertFailsWith<ConstraintFormatException> { parseMavenVersionDescriptor("1-alpha.0") }
        assertFailsWith<ConstraintFormatException> { parseMavenVersionDescriptor("1.0-alpha.0") }
    }

}