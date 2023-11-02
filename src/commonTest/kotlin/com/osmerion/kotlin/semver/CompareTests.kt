package com.osmerion.kotlin.semver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CompareTests {
    @Test
    fun testLessThanByNumbers() {
        val version = SemanticVersion.parse("5.2.3")
        assertTrue { version < SemanticVersion.parse("6.0.0") }
        assertTrue { version < SemanticVersion.parse("5.3.3") }
        assertTrue { version < SemanticVersion.parse("5.2.4") }
    }

    @Test
    fun testLessThanByPreRelease() {
        val version = SemanticVersion.parse("5.2.3-alpha.2")
        assertTrue { version < SemanticVersion.parse("5.2.3-alpha.2.a") } // by pre-release part count
        assertTrue { version < SemanticVersion.parse("5.2.3-alpha.3") } // by pre-release number comparison
        assertTrue { version < SemanticVersion.parse("5.2.3-beta") } // by pre-release alphabetical comparison
        assertTrue { version <= SemanticVersion.parse("5.2.3-alpha.2") }
    }

    @Test
    fun testPrecedenceFromSpec() {
        assertTrue { SemanticVersion.parse("1.0.0") < SemanticVersion.parse("2.0.0") }
        assertTrue { SemanticVersion.parse("2.0.0") < SemanticVersion.parse("2.1.0") }
        assertTrue { SemanticVersion.parse("2.1.0") < SemanticVersion.parse("2.1.1") }

        assertTrue { SemanticVersion.parse("1.0.0-alpha") < SemanticVersion.parse("1.0.0") }

        assertTrue { SemanticVersion.parse("1.0.0-alpha") < SemanticVersion.parse("1.0.0-alpha.1") }
        assertTrue { SemanticVersion.parse("1.0.0-alpha.1") < SemanticVersion.parse("1.0.0-alpha.beta") }
        assertTrue { SemanticVersion.parse("1.0.0-alpha.beta") < SemanticVersion.parse("1.0.0-beta") }
        assertTrue { SemanticVersion.parse("1.0.0-beta") < SemanticVersion.parse("1.0.0-beta.2") }
        assertTrue { SemanticVersion.parse("1.0.0-beta.2") < SemanticVersion.parse("1.0.0-beta.11") }
        assertTrue { SemanticVersion.parse("1.0.0-beta.11") < SemanticVersion.parse("1.0.0-rc.1") }
        assertTrue { SemanticVersion.parse("1.0.0-rc.1") < SemanticVersion.parse("1.0.0") }
    }

    @Test
    fun testCompareByPreReleaseNumberAlphabetical() {
        assertTrue { SemanticVersion.parse("5.2.3-alpha.2") < SemanticVersion.parse("5.2.3-alpha.a") }
        assertTrue { SemanticVersion.parse("5.2.3-alpha.a") > SemanticVersion.parse("5.2.3-alpha.2") }
    }

    @Test
    fun testCompareByPreReleaseAndStable() {
        assertTrue { SemanticVersion.parse("5.2.3-alpha") < SemanticVersion.parse("5.2.3") }
        assertTrue { SemanticVersion.parse("5.2.3") > SemanticVersion.parse("5.2.3-alpha") }
    }

    @Test
    fun testGreaterThanByNumbers() {
        val version = SemanticVersion.parse("5.2.3")
        assertTrue { version > SemanticVersion.parse("4.0.0") }
        assertTrue { version > SemanticVersion.parse("5.1.3") }
        assertTrue { version > SemanticVersion.parse("5.2.2") }
    }

    @Test
    fun testGreaterThanByPreRelease() {
        val version = SemanticVersion.parse("5.2.3-alpha.2")
        assertTrue { version > SemanticVersion.parse("5.2.3-alpha") } // by pre-release part count
        assertTrue { version > SemanticVersion.parse("5.2.3-alpha.1") } // by pre-release number comparison
        assertTrue { version > SemanticVersion.parse("5.2.3-a") } // by pre-release alphabetical comparison
        assertTrue { version >= SemanticVersion.parse("5.2.3-alpha.2") }
    }

    @Test
    fun testEqual() {
        assertEquals(SemanticVersion.parse("5.2.3-alpha.2"), SemanticVersion.parse("5.2.3-alpha.2"))
        assertNotEquals(SemanticVersion.parse("5.2.3-alpha.2"), SemanticVersion.parse("5.2.3-alpha.5"))
        assertEquals(SemanticVersion.parse("5.2.3"), SemanticVersion.parse("5.2.3"))
        assertNotEquals(SemanticVersion.parse("5.2.3"), SemanticVersion.parse("5.2.4"))
        assertEquals(SemanticVersion.parse("0.0.0"), SemanticVersion.parse("0.0.0"))
        assertEquals(SemanticVersion.parse("0.0.0-alpha.2").hashCode(), SemanticVersion.parse("0.0.0-alpha.2").hashCode())
        assertEquals(SemanticVersion.parse("0.0.0").hashCode(), SemanticVersion.parse("0.0.0").hashCode())
        assertFalse(SemanticVersion.parse("0.0.0").equals(null))
    }

    @Test
    fun testEqualIgnoreBuild() {
        assertEquals(SemanticVersion.parse("5.2.3-alpha.2+build.34"), SemanticVersion.parse("5.2.3-alpha.2"))
        assertEquals(SemanticVersion.parse("5.2.3-alpha.2+build.34"), SemanticVersion.parse("5.2.3-alpha.2+build.35"))
    }

    @Test
    fun testListOrder() {
        val list: List<SemanticVersion> = listOf(
            SemanticVersion.parse("1.0.1"),
            SemanticVersion.parse("1.0.1-alpha"),
            SemanticVersion.parse("1.0.1-alpha.beta"),
            SemanticVersion.parse("1.0.1-alpha.3"),
            SemanticVersion.parse("1.0.1-alpha.2"),
            SemanticVersion.parse("1.1.0"),
            SemanticVersion.parse("1.1.0+build"),
        )

        assertEquals(
            listOf(
                SemanticVersion.parse("1.0.1-alpha"),
                SemanticVersion.parse("1.0.1-alpha.2"),
                SemanticVersion.parse("1.0.1-alpha.3"),
                SemanticVersion.parse("1.0.1-alpha.beta"),
                SemanticVersion.parse("1.0.1"),
                SemanticVersion.parse("1.1.0"),
                SemanticVersion.parse("1.1.0+build"),
            ),
            list.sorted()
        )
    }
}
