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
package com.osmerion.kotlin.semver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NextVersionTests {

    @Test
    fun testVersionComponentsNullBuildMeta() {
        val version = SemanticVersion.parse("1.2.3-alpha.4+build.3")
        assertEquals("2.0.0", version.toNextMajor().toString())
        assertEquals("1.3.0", version.toNextMinor().toString())
        assertEquals("1.2.3", version.toNextPatch().toString())
    }

    @Test
    fun testNextVersionsWithoutPreRelease() {
        val version = SemanticVersion.parse("1.2.3")
        assertEquals("2.0.0", version.toNextMajor().toString())
        assertEquals("1.3.0", version.toNextMinor().toString())
        assertEquals("1.2.4", version.toNextPatch().toString())
    }

    @Test
    fun testNextVersionsWithNonNumericPreRelease() {
        val version = SemanticVersion.parse("1.2.3-alpha")
        assertEquals("2.0.0", version.toNextMajor().toString())
        assertEquals("1.3.0", version.toNextMinor().toString())
        assertEquals("1.2.3", version.toNextPatch().toString())
    }

    @Test
    fun testInvalidPreReleases() {
        val version = SemanticVersion.parse("1.2.3-alpha")
        assertFailsWith<VersionFormatException> { version.toNextMajor(preRelease = "01") }
        assertFailsWith<VersionFormatException> { version.toNextMinor(preRelease = "01") }
        assertFailsWith<VersionFormatException> { version.toNextPatch(preRelease = "01") }
    }

}