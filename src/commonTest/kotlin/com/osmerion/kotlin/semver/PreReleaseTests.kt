package com.osmerion.kotlin.semver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class PreReleaseTests {
    @Test
    fun testInvalidVersions() {
        assertFailsWith<VersionFormatException> { PreRelease(".alpha") }
        assertFailsWith<VersionFormatException> { PreRelease("alpha.") }
        assertFailsWith<VersionFormatException> { PreRelease(".alpha.") }
        assertFailsWith<VersionFormatException> { PreRelease("alpha. ") }
        assertFailsWith<VersionFormatException> { PreRelease("alpha.01") }
        assertFailsWith<VersionFormatException> { PreRelease("+alpha.01") }
        assertFailsWith<VersionFormatException> { PreRelease("%alpha") }
    }

    @Test
    fun testIncrement() {
        assertEquals("alpha-3.Beta.0", PreRelease("alpha-3.Beta").increment().toString())
        assertEquals("alpha-3.14.Beta", PreRelease("alpha-3.13.Beta").increment().toString())
        assertEquals("alpha.5.Beta.8", PreRelease("alpha.5.Beta.7").increment().toString())
    }

    @Test
    fun testEquality() {
        assertEquals("alpha-3.Beta.0", PreRelease("alpha-3.Beta.0").toString())
    }

    @Test
    fun testIdentity() {
        assertEquals("alpha-3", PreRelease("alpha-3.beta.0").identity)
        assertEquals("beta", PreRelease("beta.0").identity)
        assertEquals("3", PreRelease("3.Beta.0").identity)
        assertEquals("3", PreRelease("3.0").identity)
    }

    @Test
    fun testEquals() {
        assertEquals(PreRelease("alpha-3.Beta.0"), PreRelease("alpha-3.Beta.0"))
        assertNotEquals(PreRelease("alpha-3.Beta.0"), PreRelease("alpha-3.Beta.1"))
        assertFalse(PreRelease("alpha-3.Beta.1").equals(null))
    }

    @Test
    fun testDefault() {
        assertEquals("0", PreRelease.default.toString())
    }
}
