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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VersionTests {
    @Test
    fun testInvalidVersions() {
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("-1.0.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.-1.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("0.0.-1") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("", strict = false) }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.0-alpha") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.0-alpha.01") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("a1.0.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.a0.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.0.a0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("92233720368547758072.0.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("0.92233720368547758072.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("0.0.92233720368547758072") }
        assertFailsWith<VersionFormatException> { SemanticVersion(major = 1, minor = 2, patch = 3, preRelease = ".alpha") }
        assertFailsWith<VersionFormatException> { SemanticVersion(1, 2, 3, "alpha.") }
        assertFailsWith<VersionFormatException> { SemanticVersion(1, 2, 3, ".alpha.") }
        assertFailsWith<VersionFormatException> { SemanticVersion(1, 2, 3, "alpha. ") }
        assertFailsWith<VersionFormatException> { SemanticVersion(-1, 2, 3) }
        assertFailsWith<VersionFormatException> { SemanticVersion(1, -2, 3) }
        assertFailsWith<VersionFormatException> { SemanticVersion(1, 2, -3) }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("v1.0.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("92233720368547758072", strict = false) }

        assertFailsWith<VersionFormatException> { SemanticVersion.parse("-1.0.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.-1.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("0.0.-1") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("", strict = false) }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.0-alpha") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.0-alpha.01") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("a1.0.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.a0.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("1.0.a0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("92233720368547758072.0.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("0.92233720368547758072.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("0.0.92233720368547758072") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("v1.0.0") }
        assertFailsWith<VersionFormatException> { SemanticVersion.parse("92233720368547758072", strict = false) }
    }

    @Test
    fun testInvalidVersionsWithNull() {
        assertNull(SemanticVersion.tryParse("-1.0.0"))
        assertNull(SemanticVersion.tryParse("1.-1.0"))
        assertNull(SemanticVersion.tryParse("0.0.-1"))
        assertNull(SemanticVersion.tryParse("1"))
        assertNull(SemanticVersion.tryParse("1.0"))
        assertNull(SemanticVersion.tryParse("1.0-alpha"))
        assertNull(SemanticVersion.tryParse("1.0-alpha.01"))
        assertNull(SemanticVersion.tryParse("a1.0.0"))
        assertNull(SemanticVersion.tryParse("1.a0.0"))
        assertNull(SemanticVersion.tryParse("1.0.a0"))
        assertNull(SemanticVersion.tryParse("92233720368547758072.0.0"))
        assertNull(SemanticVersion.tryParse("0.92233720368547758072.0"))
        assertNull(SemanticVersion.tryParse("0.0.92233720368547758072"))
        assertNull(SemanticVersion.tryParse("v1.0.0"))
        assertNotNull(SemanticVersion.tryParse("v1.0.0", strict = false))
    }

    @Test
    fun testValidVersion() {
        SemanticVersion.parse("0.0.0")
        SemanticVersion.parse("1.2.3-alpha.1+build")
        SemanticVersion.parse("v1.0.0", strict = false)
        SemanticVersion.parse("1.0", strict = false)
        SemanticVersion.parse("v1", strict = false)
        SemanticVersion.parse("1", strict = false)

        assertFalse(SemanticVersion.parse("2.3.1").isPreRelease)
        assertTrue(SemanticVersion.parse("2.3.1-alpha").isPreRelease)
        assertFalse(SemanticVersion.parse("2.3.1+build").isPreRelease)
    }

    @Test
    fun testToString() {
        assertEquals("1.2.3", SemanticVersion.parse("1.2.3").toString())
        assertEquals("1.2.3-alpha.b.3", SemanticVersion.parse("1.2.3-alpha.b.3").toString())
        assertEquals("1.2.3-alpha+build", SemanticVersion.parse("1.2.3-alpha+build").toString())
        assertEquals("1.2.3+build", SemanticVersion.parse("1.2.3+build").toString())
        assertEquals("1.2.3", SemanticVersion.parse("v1.2.3", strict = false).toString())
        assertEquals("1.0.0", SemanticVersion.parse("v1", strict = false).toString())
        assertEquals("1.0.0", SemanticVersion.parse("1", strict = false).toString())
        assertEquals("1.2.0", SemanticVersion.parse("1.2", strict = false).toString())
        assertEquals("1.2.0", SemanticVersion.parse("v1.2", strict = false).toString())

        assertEquals("1.2.3-alpha+build", SemanticVersion.parse("v1.2.3-alpha+build", strict = false).toString())
        assertEquals("1.0.0-alpha+build", SemanticVersion.parse("v1-alpha+build", strict = false).toString())
        assertEquals("1.0.0-alpha+build", SemanticVersion.parse("1-alpha+build", strict = false).toString())
        assertEquals("1.2.0-alpha+build", SemanticVersion.parse("1.2-alpha+build", strict = false).toString())
        assertEquals("1.2.0-alpha+build", SemanticVersion.parse("v1.2-alpha+build", strict = false).toString())
    }

    @Test
    fun testVersionComponents() {
        with(SemanticVersion.parse("1.2.3-alpha.b.3+build")) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertEquals("alpha.b.3", preRelease)
            assertEquals("build", buildMetadata)
            assertTrue(isPreRelease)
            assertFalse(isStable)
        }
    }

    @Test
    fun testVersionComponentsOnlyNumbers() {
        with(SemanticVersion.parse("1.2.3")) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }
    }

    @Test
    fun testVersionComponentsNullPreRelease() {
        with(SemanticVersion.parse("1.2.3+build")) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertNull(preRelease)
            assertEquals("build", buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }
    }

    @Test
    fun testVersionDefault() {
        with(SemanticVersion()) {
            assertEquals(0, major)
            assertEquals(0, minor)
            assertEquals(0, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertFalse(isStable)
            assertFalse(isPreRelease)
        }

        with(SemanticVersion(major = 1)) {
            assertEquals(1, major)
            assertEquals(0, minor)
            assertEquals(0, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }

        with(SemanticVersion(major = 1, minor = 2)) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(0, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }

        with(SemanticVersion(major = 1, minor = 2, patch = 3)) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }

        with(SemanticVersion(major = 1, minor = 2, patch = 3, preRelease = "alpha")) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertEquals(preRelease, "alpha")
            assertNull(buildMetadata)
            assertFalse(isStable)
            assertTrue(isPreRelease)
        }

        with(SemanticVersion(major = 1, minor = 2, patch = 3, preRelease = "alpha", buildMetadata = "build")) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertEquals(preRelease, "alpha")
            assertEquals(buildMetadata, "build")
            assertFalse(isStable)
            assertTrue(isPreRelease)
        }
    }

    @Test
    fun testVersionComponentsNullBuildMeta() {
        with(SemanticVersion.parse("1.2.3-alpha")) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertEquals(preRelease, "alpha")
            assertNull(buildMetadata)
        }
    }

    @Test
    fun testClone() {
        assertEquals("1.2.3-alpha+build", SemanticVersion.parse("1.2.3-alpha+build").copy().toString())
        assertEquals("2.2.3-alpha+build", SemanticVersion.parse("1.2.3-alpha+build").copy(major = 2).toString())
        assertEquals("2.2.4", SemanticVersion.parse("1.2.4").copy(major = 2).toString())
        assertEquals("1.3.4", SemanticVersion.parse("1.2.4").copy(minor = 3).toString())
        assertEquals("1.2.5", SemanticVersion.parse("1.2.4").copy(patch = 5).toString())
        assertEquals("2.3.5", SemanticVersion.parse("1.2.4").copy(major = 2, minor = 3, patch = 5).toString())
        assertEquals("2.3.5-alpha", SemanticVersion.parse("1.2.4-alpha").copy(major = 2, minor = 3, patch = 5).toString())
        assertEquals("1.2.4-alpha", SemanticVersion.parse("1.2.4").copy(preRelease = "alpha").toString())
        assertEquals("1.2.4-beta", SemanticVersion.parse("1.2.4-alpha").copy(preRelease = "beta").toString())
        assertEquals("1.2.4+build", SemanticVersion.parse("1.2.4").copy(buildMetadata = "build").toString())
        assertEquals("1.2.4+build12", SemanticVersion.parse("1.2.4+build").copy(buildMetadata = "build12").toString())
        assertEquals("1.2.4-alpha+build", SemanticVersion.parse("1.2.4-alpha").copy(buildMetadata = "build").toString())
    }

    @Test
    fun testWithoutSuffixes() {
        assertEquals("1.2.3", SemanticVersion.parse("1.2.3-alpha+build").withoutSuffixes().toString())
        assertEquals("1.2.4", SemanticVersion.parse("1.2.4").withoutSuffixes().toString())
        assertEquals("1.2.4", SemanticVersion.parse("1.2.4-alpha").withoutSuffixes().toString())
        assertEquals("1.2.4", SemanticVersion.parse("1.2.4+build").withoutSuffixes().toString())
    }

    @Test
    fun testDestructuring() {
        val (major, minor, patch, preRelease, build) = SemanticVersion.parse("1.2.3-alpha+build")
        assertEquals(1, major)
        assertEquals(2, minor)
        assertEquals(3, patch)
        assertEquals("alpha", preRelease)
        assertEquals("build", build)

        val (ma, mi, pa) = SemanticVersion.parse("3.4.2")
        assertEquals(3, ma)
        assertEquals(4, mi)
        assertEquals(2, pa)
    }

    @Test
    fun testRange() {
        assertTrue(SemanticVersion.parse("1.0.1") in SemanticVersion.parse("1.0.0")..SemanticVersion.parse("1.1.0"))
        assertFalse(SemanticVersion.parse("1.1.1") in SemanticVersion.parse("1.0.0")..SemanticVersion.parse("1.1.0"))
        assertTrue(SemanticVersion.parse("1.0.0") in SemanticVersion.parse("1.0.0")..SemanticVersion.parse("1.1.0"))
        assertTrue(SemanticVersion.parse("1.0.0-alpha.3") in SemanticVersion.parse("1.0.0-alpha.2")..SemanticVersion.parse("1.0.0-alpha.5"))
        assertFalse(SemanticVersion.parse("1.0.0-alpha.1") in SemanticVersion.parse("1.0.0-alpha.2")..SemanticVersion.parse("1.0.0-alpha.5"))
        assertTrue((SemanticVersion.parse("1.0.0")..SemanticVersion.parse("1.1.0")).contains(SemanticVersion.parse("1.0.1")))
        assertTrue(SemanticVersion.parse("1.1.0-alpha") in SemanticVersion.parse("1.0.0")..SemanticVersion.parse("1.1.0"))
    }
}
