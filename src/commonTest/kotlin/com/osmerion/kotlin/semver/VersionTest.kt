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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VersionTest {

    @Test
    fun testInvalidVersions() {
        assertFailsWith<VersionFormatException> { Version.parse("-1.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.-1.0") }
        assertFailsWith<VersionFormatException> { Version.parse("0.0.-1") }
        assertFailsWith<VersionFormatException> { Version.parse("1") }
        assertFailsWith<VersionFormatException> { Version.parse("") }
        assertFailsWith<VersionFormatException> { Version.parse("", strict = false) }
        assertFailsWith<VersionFormatException> { Version.parse("1.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.0-alpha") }
        assertFailsWith<VersionFormatException> { Version.parse("1.0-alpha.01") }
        assertFailsWith<VersionFormatException> { Version.parse("a1.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.a0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.0.a0") }
        assertFailsWith<VersionFormatException> { Version.parse("92233720368547758072.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("0.92233720368547758072.0") }
        assertFailsWith<VersionFormatException> { Version.parse("0.0.92233720368547758072") }
        assertFailsWith<VersionFormatException> { Version(major = 1, minor = 2, patch = 3, preRelease = ".alpha") }
        assertFailsWith<VersionFormatException> { Version(1, 2, 3, "alpha.") }
        assertFailsWith<VersionFormatException> { Version(1, 2, 3, ".alpha.") }
        assertFailsWith<VersionFormatException> { Version(1, 2, 3, "alpha. ") }
        assertFailsWith<VersionFormatException> { Version(-1, 2, 3) }
        assertFailsWith<VersionFormatException> { Version(1, -2, 3) }
        assertFailsWith<VersionFormatException> { Version(1, 2, -3) }
        assertFailsWith<VersionFormatException> { Version.parse("v1.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("92233720368547758072", strict = false) }

        assertFailsWith<VersionFormatException> { Version.parse("-1.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.-1.0") }
        assertFailsWith<VersionFormatException> { Version.parse("0.0.-1") }
        assertFailsWith<VersionFormatException> { Version.parse("1") }
        assertFailsWith<VersionFormatException> { Version.parse("") }
        assertFailsWith<VersionFormatException> { Version.parse("", strict = false) }
        assertFailsWith<VersionFormatException> { Version.parse("1.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.0-alpha") }
        assertFailsWith<VersionFormatException> { Version.parse("1.0-alpha.01") }
        assertFailsWith<VersionFormatException> { Version.parse("a1.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.a0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.0.a0") }
        assertFailsWith<VersionFormatException> { Version.parse("92233720368547758072.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("0.92233720368547758072.0") }
        assertFailsWith<VersionFormatException> { Version.parse("0.0.92233720368547758072") }
        assertFailsWith<VersionFormatException> { Version.parse("v1.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("92233720368547758072", strict = false) }
    }

    @Test
    fun testInvalidVersionsWithNull() {
        assertNull(Version.tryParse("-1.0.0"))
        assertNull(Version.tryParse("1.-1.0"))
        assertNull(Version.tryParse("0.0.-1"))
        assertNull(Version.tryParse("1"))
        assertNull(Version.tryParse("1.0"))
        assertNull(Version.tryParse("1.0-alpha"))
        assertNull(Version.tryParse("1.0-alpha.01"))
        assertNull(Version.tryParse("a1.0.0"))
        assertNull(Version.tryParse("1.a0.0"))
        assertNull(Version.tryParse("1.0.a0"))
        assertNull(Version.tryParse("92233720368547758072.0.0"))
        assertNull(Version.tryParse("0.92233720368547758072.0"))
        assertNull(Version.tryParse("0.0.92233720368547758072"))
        assertNull(Version.tryParse("v1.0.0"))
        assertNotNull(Version.tryParse("v1.0.0", strict = false))
    }

    @Test
    fun testValidVersion() {
        Version.parse("0.0.0")
        Version.parse("1.2.3-alpha.1+build")
        Version.parse("v1.0.0", strict = false)
        Version.parse("1.0", strict = false)
        Version.parse("v1", strict = false)
        Version.parse("1", strict = false)

        assertFalse(Version.parse("2.3.1").isPreRelease)
        assertTrue(Version.parse("2.3.1-alpha").isPreRelease)
        assertFalse(Version.parse("2.3.1+build").isPreRelease)
    }

    @Test
    fun testToString() {
        assertEquals("1.2.3", Version.parse("1.2.3").toString())
        assertEquals("1.2.3-alpha.b.3", Version.parse("1.2.3-alpha.b.3").toString())
        assertEquals("1.2.3-alpha+build", Version.parse("1.2.3-alpha+build").toString())
        assertEquals("1.2.3+build", Version.parse("1.2.3+build").toString())
        assertEquals("1.2.3", Version.parse("v1.2.3", strict = false).toString())
        assertEquals("1.0.0", Version.parse("v1", strict = false).toString())
        assertEquals("1.0.0", Version.parse("1", strict = false).toString())
        assertEquals("1.2.0", Version.parse("1.2", strict = false).toString())
        assertEquals("1.2.0", Version.parse("v1.2", strict = false).toString())

        assertEquals("1.2.3-alpha+build", Version.parse("v1.2.3-alpha+build", strict = false).toString())
        assertEquals("1.0.0-alpha+build", Version.parse("v1-alpha+build", strict = false).toString())
        assertEquals("1.0.0-alpha+build", Version.parse("1-alpha+build", strict = false).toString())
        assertEquals("1.2.0-alpha+build", Version.parse("1.2-alpha+build", strict = false).toString())
        assertEquals("1.2.0-alpha+build", Version.parse("v1.2-alpha+build", strict = false).toString())
    }

    @Test
    fun testVersionComponents() {
        with(Version.parse("1.2.3-alpha.b.3+build")) {
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
        with(Version.parse("1.2.3")) {
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
        with(Version.parse("1.2.3+build")) {
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
        with(Version()) {
            assertEquals(0, major)
            assertEquals(0, minor)
            assertEquals(0, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertFalse(isStable)
            assertFalse(isPreRelease)
        }

        with(Version(major = 1)) {
            assertEquals(1, major)
            assertEquals(0, minor)
            assertEquals(0, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }

        with(Version(major = 1, minor = 2)) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(0, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }

        with(Version(major = 1, minor = 2, patch = 3)) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }

        with(Version(major = 1, minor = 2, patch = 3, preRelease = "alpha")) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertEquals(preRelease, "alpha")
            assertNull(buildMetadata)
            assertFalse(isStable)
            assertTrue(isPreRelease)
        }

        with(Version(major = 1, minor = 2, patch = 3, preRelease = "alpha", buildMetadata = "build")) {
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
        with(Version.parse("1.2.3-alpha")) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertEquals(preRelease, "alpha")
            assertNull(buildMetadata)
        }
    }

    @Test
    fun testClone() {
        assertEquals("1.2.3-alpha+build", Version.parse("1.2.3-alpha+build").copy().toString())
        assertEquals("2.2.3-alpha+build", Version.parse("1.2.3-alpha+build").copy(major = 2).toString())
        assertEquals("2.2.4", Version.parse("1.2.4").copy(major = 2).toString())
        assertEquals("1.3.4", Version.parse("1.2.4").copy(minor = 3).toString())
        assertEquals("1.2.5", Version.parse("1.2.4").copy(patch = 5).toString())
        assertEquals("2.3.5", Version.parse("1.2.4").copy(major = 2, minor = 3, patch = 5).toString())
        assertEquals("2.3.5-alpha", Version.parse("1.2.4-alpha").copy(major = 2, minor = 3, patch = 5).toString())
        assertEquals("1.2.4-alpha", Version.parse("1.2.4").copy(preRelease = "alpha").toString())
        assertEquals("1.2.4-beta", Version.parse("1.2.4-alpha").copy(preRelease = "beta").toString())
        assertEquals("1.2.4+build", Version.parse("1.2.4").copy(buildMetadata = "build").toString())
        assertEquals("1.2.4+build12", Version.parse("1.2.4+build").copy(buildMetadata = "build12").toString())
        assertEquals("1.2.4-alpha+build", Version.parse("1.2.4-alpha").copy(buildMetadata = "build").toString())
    }

    @Test
    fun testRemovePreRelease() {
        assertEquals("1.2.3+build", Version.parse("1.2.3-alpha+build").removePreRelease().toString())
        assertEquals("1.2.4", Version.parse("1.2.4").removePreRelease().toString())
        assertEquals("1.2.4", Version.parse("1.2.4-alpha").removePreRelease().toString())
        assertEquals("1.2.4+build", Version.parse("1.2.4+build").removePreRelease().toString())
    }

    @Test
    fun testRemoveBuildMetadata() {
        assertEquals("1.2.3-alpha", Version.parse("1.2.3-alpha+build").removeBuildMetadata().toString())
        assertEquals("1.2.4", Version.parse("1.2.4").removeBuildMetadata().toString())
        assertEquals("1.2.4-alpha", Version.parse("1.2.4-alpha").removeBuildMetadata().toString())
        assertEquals("1.2.4", Version.parse("1.2.4+build").removeBuildMetadata().toString())
    }

    @Test
    fun testToNormalVersion() {
        assertEquals("1.2.3", Version.parse("1.2.3-alpha+build").toNormalVersion().toString())
        assertEquals("1.2.4", Version.parse("1.2.4").toNormalVersion().toString())
        assertEquals("1.2.4", Version.parse("1.2.4-alpha").toNormalVersion().toString())
        assertEquals("1.2.4", Version.parse("1.2.4+build").toNormalVersion().toString())
    }

    @Test
    fun testDestructuring() {
        val (major, minor, patch, preRelease, build) = Version.parse("1.2.3-alpha+build")
        assertEquals(1, major)
        assertEquals(2, minor)
        assertEquals(3, patch)
        assertEquals("alpha", preRelease)
        assertEquals("build", build)

        val (ma, mi, pa) = Version.parse("3.4.2")
        assertEquals(3, ma)
        assertEquals(4, mi)
        assertEquals(2, pa)
    }

    @Test
    fun testRange() {
        assertTrue(Version.parse("1.0.1") in Version.parse("1.0.0")..Version.parse("1.1.0"))
        assertFalse(Version.parse("1.1.1") in Version.parse("1.0.0")..Version.parse("1.1.0"))
        assertTrue(Version.parse("1.0.0") in Version.parse("1.0.0")..Version.parse("1.1.0"))
        assertTrue(Version.parse("1.0.0-alpha.3") in Version.parse("1.0.0-alpha.2")..Version.parse("1.0.0-alpha.5"))
        assertFalse(Version.parse("1.0.0-alpha.1") in Version.parse("1.0.0-alpha.2")..Version.parse("1.0.0-alpha.5"))
        assertTrue((Version.parse("1.0.0")..Version.parse("1.1.0")).contains(Version.parse("1.0.1")))
        assertTrue(Version.parse("1.1.0-alpha") in Version.parse("1.0.0")..Version.parse("1.1.0"))
    }

}
