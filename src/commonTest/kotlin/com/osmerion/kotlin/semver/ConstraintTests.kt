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

import com.osmerion.kotlin.semver.internal.constraints.Condition
import com.osmerion.kotlin.semver.internal.constraints.Op
import com.osmerion.kotlin.semver.internal.constraints.Range
import com.osmerion.kotlin.semver.internal.constraints.VersionDescriptor
import com.osmerion.kotlin.semver.internal.constraints.toOperator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConstraintTests {

    @Test
    fun testInvalidConstraints() {
        assertFailsWith<ConstraintFormatException> { SemanticVersionConstraint.parse("a") }
        assertFailsWith<ConstraintFormatException> { SemanticVersionConstraint.parse("||") }
        assertFailsWith<ConstraintFormatException> { SemanticVersionConstraint.parse(">1.a") }
        assertFailsWith<ConstraintFormatException> { SemanticVersionConstraint.parse("1.1-3") }
        assertFailsWith<ConstraintFormatException> { SemanticVersionConstraint.parse(">0-alpha") }
        assertFailsWith<ConstraintFormatException> { SemanticVersionConstraint.parse(">=0.0-0") }
        assertFailsWith<ConstraintFormatException> { SemanticVersionConstraint.parse(">=1.2a") }
    }

    @Test
    fun testInvalidConstraintsNull() {
        assertNull(SemanticVersionConstraint.tryParse("a"))
        assertNull(SemanticVersionConstraint.tryParse("||"))
        assertNull(SemanticVersionConstraint.tryParse(">1.a"))
        assertNull(SemanticVersionConstraint.tryParse("1.1-3"))
        assertNull(SemanticVersionConstraint.tryParse(">0-alpha"))
        assertNull(SemanticVersionConstraint.tryParse(">=0.0-0"))
        assertNull(SemanticVersionConstraint.tryParse(">=1.2a"))
    }

    @Test
    fun testConstraintsEquals() {
        assertEquals(SemanticVersionConstraint.parse(">1.0"), SemanticVersionConstraint.parse("> 1.0"))
        assertEquals(SemanticVersionConstraint.parse(">=1.1.0-0"), SemanticVersionConstraint.parse(">1.0"))
        assertEquals(SemanticVersionConstraint.parse(">=1.0.0 <2.1.0-0"), SemanticVersionConstraint.parse("1.0 - 2.0"))
        assertEquals(SemanticVersionConstraint.parse("<1.0.0 || >=1.1.0-0"), SemanticVersionConstraint.parse("!=1.0"))
    }

    @Test
    fun testSatisfiesAll() {
        val constraints = listOf("!=1.2.4", "=1.2.3", ">1.0.0").map { SemanticVersionConstraint.parse(it) }
        assertTrue(SemanticVersion.parse("1.2.3") satisfiesAll constraints)
        assertFalse(SemanticVersion.parse("1.2.4") satisfiesAll constraints)

        val versions = listOf("1.0.0", "1.0.1").map(SemanticVersion::parse)
        assertTrue(SemanticVersionConstraint.parse(">=1.0.0") isSatisfiedByAll versions)
        assertFalse(SemanticVersionConstraint.parse(">=1.0.1") isSatisfiedByAll versions)
    }

    @Test
    fun testSatisfiesAny() {
        val constraints = listOf("!=1.2.4", "=1.2.3", ">1.0.0").map { SemanticVersionConstraint.parse(it) }
        assertTrue(SemanticVersion.parse("1.2.3") satisfiesAny constraints)
        assertTrue(SemanticVersion.parse("1.2.4") satisfiesAny constraints)

        val versions = listOf("1.0.0", "1.0.1").map(SemanticVersion::parse)
        assertTrue(SemanticVersionConstraint.parse(">=1.0.0") isSatisfiedByAny versions)
        assertTrue(SemanticVersionConstraint.parse(">=1.0.1") isSatisfiedByAny versions)
    }

    @Test
    fun testToOperator() {
        assertEquals(Op.EQUAL, "=".toOperator())
        assertEquals(Op.NOT_EQUAL, "!=".toOperator())
        assertEquals(Op.GREATER_THAN, ">".toOperator())
        assertEquals(Op.LESS_THAN, "<".toOperator())
        assertEquals(Op.GREATER_THAN_OR_EQUAL, ">=".toOperator())
        assertEquals(Op.GREATER_THAN_OR_EQUAL, "=>".toOperator())
        assertEquals(Op.LESS_THAN_OR_EQUAL, "<=".toOperator())
        assertEquals(Op.LESS_THAN_OR_EQUAL, "=<".toOperator())
        assertEquals(Op.EQUAL, "non-existing".toOperator())
    }

    @Test
    fun testCondition() {
        val version = SemanticVersion.parse("1.0.0")
        assertEquals("${Op.NOT_EQUAL}1.0.0", Condition(Op.EQUAL, version).opposite())
        assertEquals("${Op.EQUAL}1.0.0", Condition(Op.NOT_EQUAL, version).opposite())
        assertEquals("${Op.GREATER_THAN_OR_EQUAL}1.0.0", Condition(Op.LESS_THAN, version).opposite())
        assertEquals("${Op.GREATER_THAN}1.0.0", Condition(Op.LESS_THAN_OR_EQUAL, version).opposite())
        assertEquals("${Op.LESS_THAN_OR_EQUAL}1.0.0", Condition(Op.GREATER_THAN, version).opposite())
        assertEquals("${Op.LESS_THAN}1.0.0", Condition(Op.GREATER_THAN_OR_EQUAL, version).opposite())

        assertTrue(Condition(Op.EQUAL, version).isSatisfiedBy(SemanticVersion.parse("1.0.0")))
        assertTrue(Condition(Op.NOT_EQUAL, version).isSatisfiedBy(SemanticVersion.parse("1.2.0")))
        assertTrue(Condition(Op.LESS_THAN, version).isSatisfiedBy(SemanticVersion.parse("0.1.0")))
        assertTrue(Condition(Op.LESS_THAN_OR_EQUAL, version).isSatisfiedBy(SemanticVersion.parse("1.0.0")))
        assertTrue(Condition(Op.GREATER_THAN, version).isSatisfiedBy(SemanticVersion.parse("1.0.1")))
        assertTrue(Condition(Op.GREATER_THAN_OR_EQUAL, version).isSatisfiedBy(SemanticVersion.parse("1.0.0")))
    }

    @Test
    fun testRange() {
        val start = Condition(Op.GREATER_THAN, SemanticVersion.parse("1.0.0"))
        val end = Condition(Op.LESS_THAN, SemanticVersion.parse("1.1.0"))
        assertEquals("<=1.0.0 || >=1.1.0", Range(start, end, Op.EQUAL).opposite())
        assertEquals(">1.0.0 <1.1.0", Range(start, end, Op.NOT_EQUAL).opposite())
        assertEquals(">1.0.0", Range(start, end, Op.LESS_THAN).opposite())
        assertEquals(">=1.1.0", Range(start, end, Op.LESS_THAN_OR_EQUAL).opposite())
        assertEquals("<1.1.0", Range(start, end, Op.GREATER_THAN).opposite())
        assertEquals("<=1.0.0", Range(start, end, Op.GREATER_THAN_OR_EQUAL).opposite())

        assertTrue(Range(start, end, Op.EQUAL).isSatisfiedBy(SemanticVersion.parse("1.0.1")))
        assertTrue(Range(start, end, Op.NOT_EQUAL).isSatisfiedBy(SemanticVersion.parse("1.2.0")))
        assertFalse(Range(start, end, Op.LESS_THAN).isSatisfiedBy(SemanticVersion.parse("1.1.1")))
        assertTrue(Range(start, end, Op.LESS_THAN_OR_EQUAL).isSatisfiedBy(SemanticVersion.parse("1.0.0")))
        assertTrue(Range(start, end, Op.GREATER_THAN).isSatisfiedBy(SemanticVersion.parse("1.2.0")))
        assertTrue(Range(start, end, Op.GREATER_THAN_OR_EQUAL).isSatisfiedBy(SemanticVersion.parse("1.0.1")))
    }

    @Test
    fun testDescriptor() {
        assertEquals("1.2.3-pr+b", VersionDescriptor("1", "2", "3", "pr", "b").toString())
        assertEquals("1", VersionDescriptor("1", null, null).toString())
        assertEquals("1.1", VersionDescriptor("1", "1", null).toString())
        assertEquals("1.1.1", VersionDescriptor("1", "1", "1").toString())

        val desc1 = VersionDescriptor("a", "b", "c")
        assertFailsWith<ConstraintFormatException> { desc1.major }
        assertFailsWith<ConstraintFormatException> { desc1.minor }
        assertFailsWith<ConstraintFormatException> { desc1.patch }

        val desc2 = VersionDescriptor("a", null, null)
        assertFailsWith<ConstraintFormatException> { desc2.major }
        assertFailsWith<ConstraintFormatException> { desc2.minor }
        assertFailsWith<ConstraintFormatException> { desc2.patch }
    }

    @Test
    fun testEquals() {
        assertEquals(SemanticVersionConstraint.parse("> 0.0.0"), SemanticVersionConstraint.parse(">0.0.0"))
        assertEquals(SemanticVersionConstraint.parse("1.2 - 2.0"), SemanticVersionConstraint.parse(">=1.2.0 <2.1.0-0"))
        assertEquals(SemanticVersionConstraint.parse("> 0.1.0").hashCode(),
            SemanticVersionConstraint.parse(">0.1.0").hashCode())
        assertEquals(SemanticVersionConstraint.parse("1.2 - 2.0").hashCode(),
            SemanticVersionConstraint.parse(">=1.2.0 <2.1.0-0").hashCode())
        assertFalse(SemanticVersionConstraint.parse("~1").equals(null))
    }

    @Test
    fun testSatisfies() {
        val data: List<Pair<String, String>> = listOf(
            Pair("<\t1.0.0", "0.1.2"),
            Pair("1.2.3", "1.2.3"),
            Pair("=1.2.3", "1.2.3"),
            Pair("!=1.2.3", "1.2.4"),
            Pair("1.0.0 - 2.0.0", "1.2.3"),
            Pair("^1.2.3+build", "1.2.3"),
            Pair("^1.2.3+build", "1.3.0"),
            Pair("x - 1.0.0", "0.9.7"),
            Pair("x - 1.x", "0.9.7"),
            Pair("1.0.0 - x", "1.9.7"),
            Pair("1.x - x", "1.9.7"),
            Pair("1.1 - 2", "1.1.1"),
            Pair("1 - 2", "2.0.0-alpha"),
            Pair("1 - 2", "1.0.0"),
            Pair("1.0 - 2", "1.0.0"),
            Pair("1.2.3-alpha+beta - 2.4.5-alpha+beta", "1.2.3"),
            Pair("1.2.3-alpha+beta - 2.4.5-alpha+beta", "1.2.3-alpha.2"),
            Pair("1.2.3-alpha+beta - 2.4.5-alpha+beta", "2.4.5-alpha"),
            Pair("1.2.3+beta - 2.4.3+beta", "1.2.3"),
            Pair("1.0.0", "1.0.0"),
            Pair(">=1.0.0", "1.0.0"),
            Pair(">=1.0.0", "1.0.1"),
            Pair(">=1.0.0", "1.1.0"),
            Pair(">1.0.0", "1.0.1"),
            Pair(">1.0.0", "1.1.0"),
            Pair("<=2.0.0", "2.0.0"),
            Pair("<=2.0.0", "1.9.9"),
            Pair("<=2.0.0", "0.1.2"),
            Pair("<2.0.0", "1.9.9"),
            Pair("<2.0.0", "0.1.2"),
            Pair(">= 1.0.0", "1.0.0"),
            Pair(">=  1.0.0", "1.0.1"),
            Pair(">=   1.0.0", "1.1.0"),
            Pair("> 1.0.0", "1.0.1"),
            Pair(">  1.0.0", "1.1.0"),
            Pair("<=   2.0.0", "2.0.0"),
            Pair("<= 2.0.0", "1.9.9"),
            Pair("<=  2.0.0", "0.1.2"),
            Pair("<    2.0.0", "1.9.9"),
            Pair(">=0.1.2", "0.1.2"),
            Pair(">1.1 <2", "1.2.1"),
            Pair("0.1.2 || 1.2.4", "1.2.4"),
            Pair("0.1.2 | 1.2.4", "1.2.4"),
            Pair(">=0.1.2 || <0.0.1", "0.0.0"),
            Pair(">=0.1.2 || <0.0.1", "0.1.2"),
            Pair(">=0.1.2 || <0.0.1", "0.1.3"),
            Pair(">=0.1.2 | <0.0.1", "0.0.0"),
            Pair(">=0.1.2 | <0.0.1", "0.1.2"),
            Pair(">=0.1.2 | <0.0.1", "0.1.3"),
            Pair(">=0.1.2|<0.0.1", "0.0.0"),
            Pair(">=0.1.2|<0.0.1", "0.1.2"),
            Pair(">=0.1.2|<0.0.1", "0.1.3"),
            Pair(">=1.1 <2 !=1.2.3 || > 3", "4.1.2"),
            Pair(">=1.1 <2 !=1.2.3 || >= 3", "3.0.0"),
            Pair(">=1", "1.0.0"),
            Pair(">= 1", "1.0.0"),
            Pair("<1.2", "1.1.1"),
            Pair("< 1.2", "1.1.1"),
            Pair("=0.7.x", "0.7.2"),
            Pair("<=0.7.x", "0.7.2"),
            Pair(">=0.7.x", "0.7.2"),
            Pair("<=0.7.x", "0.6.2"),
            Pair("2.x.x", "2.1.3"),
            Pair("1.2.x", "1.2.3"),
            Pair("1.2.x || 2.x", "2.1.3"),
            Pair("1.2.x || 2.x", "1.2.3"),
            Pair("4.1", "4.1.0"),
            Pair("4.1.x", "4.1.3"),
            Pair("1.x", "1.4.0"),
            Pair("x", "1.2.3"),
            Pair("2.*.*", "2.1.3"),
            Pair("1.2.*", "1.2.3"),
            Pair("1.2.* || 2.*", "2.1.3"),
            Pair("1.2.* || 2.*", "1.2.3"),
            Pair("*", "1.2.3"),
            Pair(">=*", "0.2.4"),
            Pair("*", "1.0.0-beta"),
            Pair("2", "2.1.2"),
            Pair("2.3", "2.3.1"),
            Pair("~0.0.1", "0.0.1"),
            Pair("~0.0.1", "0.0.2"),
            Pair("~x", "0.0.9"),
            Pair("~2", "2.0.9"),
            Pair("~2.4", "2.4.0"),
            Pair("~2.4", "2.4.5"),
            Pair("~>3.2.1", "3.2.2"),
            Pair("~1", "1.2.3"),
            Pair("~>1", "1.2.3"),
            Pair("~> 1", "1.2.3"),
            Pair("~1.0", "1.0.2"),
            Pair("~ 1.0", "1.0.2"),
            Pair("~ 1.0.3", "1.0.12"),
            Pair("~ 1.0.3-alpha", "1.0.12"),
            Pair("~0.5.4-alpha", "0.5.5"),
            Pair("~0.5.4-alpha", "0.5.4"),
            Pair("~1.2.1 >=1.2.3", "1.2.3"),
            Pair("~1.2.1 =1.2.3", "1.2.3"),
            Pair("~1.2.1 1.2.3", "1.2.3"),
            Pair("~1.2.1 >=1.2.3 1.2.3", "1.2.3"),
            Pair("~1.2.1 1.2.3 >=1.2.3", "1.2.3"),
            Pair("~1.2.1 1.2.3", "1.2.3"),
            Pair("~*", "2.1.1"),
            Pair("~1", "1.3.5"),
            Pair("~1.x", "1.3.5"),
            Pair("~1.3.5-alpha", "1.3.5-beta"),
            Pair("~1.x", "1.2.3"),
            Pair("~1.1", "1.1.1"),
            Pair("~1.2.3", "1.2.5"),
            Pair("~0.0.0", "0.0.1"),
            Pair("~1.2.3-beta.2", "1.2.4-beta.2"),
            Pair(">=1.2.1 1.2.3", "1.2.3"),
            Pair("1.2.3 >=1.2.1", "1.2.3"),
            Pair(">=1.2.3 >=1.2.1", "1.2.3"),
            Pair(">=1.2.1 >=1.2.3", "1.2.3"),
            Pair(">=1.2", "1.2.8"),
            Pair("^1.2.3", "1.8.1"),
            Pair("^0.1.2", "0.1.2"),
            Pair("^0.1", "0.1.2"),
            Pair("^0.0.1", "0.0.1"),
            Pair("^1.2", "1.4.2"),
            Pair("^1.2 ^1", "1.4.2"),
            Pair("^1.2.3-alpha", "1.2.3-alpha"),
            Pair("^1.2.0-alpha", "1.2.0-alpha"),
            Pair("^0.0.1-alpha", "0.0.1-beta"),
            Pair("^0.0.1-alpha", "0.0.1"),
            Pair("^0.1.1-alpha", "0.1.1-beta"),
            Pair("^x", "1.2.3"),
            Pair("<=7.x", "7.9.9"),
            Pair("2.x", "2.0.0"),
            Pair("2.x", "2.1.0-alpha.0"),
            Pair("1.1.x", "1.1.0"),
            Pair("1.1.x", "1.1.1-a"),
            Pair("^1.0.0-0", "1.0.1-beta"),
            Pair("^1.0.0-beta", "1.0.1-beta"),
            Pair("^1.0.0", "1.0.1-beta"),
            Pair("^1.0.0", "1.1.0-beta"),
            Pair("^1.2.3", "1.8.9"),
            Pair("^1.2.0-alpha.0", "1.2.1-alpha.0"),
            Pair("^1.2.0-alpha.0", "1.2.1-alpha.1"),
            Pair("^1.2", "1.8.9"),
            Pair("^1", "1.8.9"),
            Pair("^0.2.3", "0.2.5"),
            Pair("^0.2", "0.2.5"),
            Pair("^0.0.3", "0.0.3"),
            Pair("^0.0", "0.0.3"),
            Pair("^0", "0.2.3"),
            Pair("^0.2.3-beta.2", "0.2.3-beta.4"),
            Pair("^1.1", "1.1.1"),
            Pair("^1.x", "1.1.1"),
            Pair("^1.1.0", "1.1.1-alpha.1"),
            Pair("^1.1.1-alpha", "1.1.1-beta"),
            Pair("^0.1.2-alpha.1", "0.1.2-alpha.1"),
            Pair("^0.1.2-alpha.1", "0.1.3-alpha.1"),
            Pair("^0.0.1", "0.0.1"),
            Pair("=0.7.x", "0.7.0"),
            Pair(">=0.7.x", "0.7.0"),
            Pair("<=0.7.x", "0.7.0"),
            Pair(">=1.0.0 <=1.1.0", "1.1.0-alpha"),
            Pair("= 2.0", "2.0.0"),
            Pair("!=1.1", "1.0.0"),
            Pair("!=1.1", "1.2.3"),
            Pair("!=1.x", "2.1.0"),
            Pair("!=1.x", "1.0.0-alpha"),
            Pair("!=1.1.x", "1.0.0"),
            Pair("!=1.1.x", "1.2.3"),
            Pair(">=1.1", "4.1.0"),
            Pair("<=1.1", "1.1.0"),
            Pair("<=1.1", "0.1.0"),
            Pair(">=1.1", "1.1.0"),
            Pair("<=1.1", "1.1.1"),
            Pair("<=1.x", "1.1.0"),
            Pair(">1.1", "4.1.0"),
            Pair("<1.1", "0.1.0"),
            Pair("<2.x", "1.1.1"),
            Pair("<1.2.x", "1.1.1"),
        )

        data.forEach {
            assertTrue { SemanticVersion.parse(it.second) satisfies SemanticVersionConstraint.parse(it.first) }
        }
    }

    @Test
    fun testNotSatisfies() {
        val data: List<Pair<String, String>> = listOf(
            Pair("~1.2.3-alpha.2", "1.3.4-alpha.2"),
            Pair("^1.2.3", "2.8.9"),
            Pair("^1.2.3", "1.2.1"),
            Pair("^1.1.0", "2.1.0"),
            Pair("^1.2.0", "2.2.1"),
            Pair("^1.2.0-alpha.2", "1.2.0-alpha.1"),
            Pair("^1.2", "2.8.9"),
            Pair("^1", "2.8.9"),
            Pair("^0.2.3", "0.5.6"),
            Pair("^0.2", "0.5.6"),
            Pair("^0.0.3", "0.0.4"),
            Pair("^0.0", "0.1.4"),
            Pair("^0.0", "1.0.4"),
            Pair("^0", "1.1.4"),
            Pair("^0.0.1", "0.0.2-alpha"),
            Pair("^0.0.1", "0.0.2"),
            Pair("^1.2.3", "2.0.0-alpha"),
            Pair("^1.2.3", "1.2.2"),
            Pair("^1.2", "1.1.9"),
            Pair("^1.0.0", "1.0.0-alpha"),
            Pair("^1.0.0", "2.0.0-alpha"),
            Pair("^1.2.3-beta", "2.0.0"),
            Pair("^1.0.0", "2.0.0-alpha"),
            Pair("^1.0.0", "2.0.0-alpha"),
            Pair("^1.2.3+build", "2.0.0"),
            Pair("^1.2.3+build", "1.2.0"),
            Pair("^1.2.3", "1.2.3-beta"),
            Pair("^1.2", "1.2.0-beta"),
            Pair("^1.1", "1.1.0-alpha"),
            Pair("^1.1.1-beta", "1.1.1-alpha"),
            Pair("^1.1", "3.0.0"),
            Pair("^2.x", "1.1.1"),
            Pair("^1.x", "2.1.1"),
            Pair("^0.0.1", "0.1.3"),
            Pair("1 - 2", "3.0.0-alpha"),
            Pair("1 - 2", "1.0.0-alpha"),
            Pair("1.0 - 2", "1.0.0-alpha"),
            Pair("1.0.0 - 2.0.0", "2.2.3"),
            Pair("1.2.3+alpha - 2.4.3+alpha", "1.2.3-alpha.1"),
            Pair("1.2.3+alpha - 2.4.3-alpha", "2.4.3-alpha.1"),
            Pair("1.1.x", "1.0.0-alpha"),
            Pair("1.1.x", "1.1.0-alpha"),
            Pair("1.1.x", "1.2.0-alpha"),
            Pair("1.1.x", "1.2.0-alpha"),
            Pair("1.1.x", "1.0.0-alpha"),
            Pair("1.x", "1.0.0-alpha"),
            Pair("1.x", "0.0.0-alpha"),
            Pair("1.x", "2.0.0-alpha"),
            Pair(">1.1", "1.1.0"),
            Pair("<1.1", "1.1.0"),
            Pair("<1.1", "1.1.1"),
            Pair("<1.x", "1.1.1"),
            Pair("<1.x", "2.1.1"),
            Pair("<1.1.x", "1.2.1"),
            Pair("<1.1.x", "1.1.1"),
            Pair(">=1.1", "0.0.9"),
            Pair("<=2.x", "3.1.0"),
            Pair("<=1.1.x", "1.2.1"),
            Pair(">1.1 <2", "1.1.1"),
            Pair(">1.1 <3", "4.3.2"),
            Pair(">=1.1 <2 !=1.1.1", "1.1.1"),
            Pair(">=1.1 <2 !=1.1.1 || > 3", "1.1.1"),
            Pair(">=1.1 <2 !=1.1.1 || > 3", "3.1.2"),
            Pair(">=1.1 <2 !=1.1.1 || > 3", "3.0.0"),
            Pair("~1", "2.1.1"),
            Pair("~1", "2.0.0-alpha"),
            Pair("~1.x", "2.1.1"),
            Pair("~1.x", "2.0.0-alpha"),
            Pair("~1.3.6-alpha", "1.3.5-beta"),
            Pair("~1.3.5-beta", "1.3.5-alpha"),
            Pair("~1.2.3", "1.2.2"),
            Pair("~1.2.3", "1.3.2"),
            Pair("~1.1", "1.2.3"),
            Pair("~1.3", "2.4.5"),
            Pair(">1.2", "1.2.0"),
            Pair("<=1.2.3", "1.2.4-beta"),
            Pair("^1.2.3", "1.2.3-beta"),
            Pair("=0.7.x", "0.7.0-alpha"),
            Pair(">=0.7.x", "0.7.0-alpha"),
            Pair("<=0.7.x", "0.8.0-alpha"),
            Pair("1", "1.0.0-beta"),
            Pair("<1", "1.0.1"),
            Pair("< 1", "1.0.1-beta"),
            Pair("1.0.0", "1.0.1"),
            Pair(">=1.0.0", "0.0.0"),
            Pair(">=1.0.0", "0.0.1"),
            Pair(">=1.0.0", "0.1.0"),
            Pair(">1.0.0", "0.0.1"),
            Pair(">1.0.0", "0.1.0"),
            Pair("<=2.0.0", "3.0.0"),
            Pair("=<2.0.0", "2.1.0"),
            Pair("=<2.0.0", "2.0.1"),
            Pair("<2.0.0", "2.0.0"),
            Pair("<2.0.0", "2.0.1"),
            Pair(">=0.1.2", "0.1.1"),
            Pair("0.1.1 || 1.2.4", "1.2.3"),
            Pair(">=0.1.0 || <0.0.1", "0.0.1"),
            Pair(">=0.1.1 || <0.0.1", "0.1.0"),
            Pair("2.x.x", "1.1.3",),
            Pair("2.x.x", "3.1.3"),
            Pair("1.2.X", "1.3.3"),
            Pair("1.2.X || 2.x", "3.1.3"),
            Pair("1.2.X || 2.x", "1.1.3"),
            Pair("2.*.*", "1.1.3"),
            Pair("2.*.*", "3.1.3"),
            Pair("1.2.*", "1.3.3"),
            Pair("2", "1.1.3"),
            Pair("2", "3.1.3"),
            Pair("1.2", "1.3.3"),
            Pair("1.2.* || 2.*", "3.1.3"),
            Pair("1.2.* || 2.*", "1.1.3"),
            Pair("2", "1.1.2"),
            Pair("2.3", "2.4.1"),
            Pair("<1", "1.0.0"),
            Pair("=>1.2", "1.1.1"),
            Pair("1", "2.0.0-beta"),
            Pair("~0.1.1-alpha.2", "0.1.1-alpha.1"),
            Pair("=0.1.x", "0.2.0"),
            Pair(">=0.1.x", "0.0.1"),
            Pair("<0.1.x", "0.1.1"),
            Pair("<1.2.3", "1.2.4-beta"),
            Pair("=1.2.3", "1.2.3-beta"),
            Pair(">1.2", "1.2.8"),
            Pair("2.x", "3.0.0-beta.0"),
            Pair(">=1.0.0 <1.1.0", "1.1.0"),
            Pair(">=1.0.0 <1.1.0", "1.1.0"),
            Pair(">=1.0.0 <1.1.0-beta", "1.1.0-beta"),
            Pair("=2.0.0", "1.2.3"),
            Pair("=2.0", "1.2.3"),
            Pair("= 2.0", "1.2.3"),
            Pair("!=4.1", "4.1.0"),
            Pair("!=4.x", "4.1.0"),
            Pair("!=4.2.x", "4.2.3"),
            Pair("!=1.1.0", "1.1.0"),
            Pair("!=1.1", "1.1.0"),
            Pair("!=1.1", "1.1.1"),
            Pair("!=1.1", "1.1.1-alpha"),
            Pair("<1", "1.1.0"),
            Pair("<1.1", "1.1.0"),
            Pair("<1.1", "1.1.1"),
            Pair("<=1", "2.0.0"),
            Pair("<=1.1", "1.2.3"),
            Pair(">1.1", "1.1.0"),
            Pair(">0", "0.0.0"),
            Pair(">0", "0.0.1-alpha"),
            Pair(">0.0", "0.0.1-alpha"),
            Pair(">0", "0.0.0-alpha"),
            Pair(">1", "1.1.0"),
            Pair(">1.1", "1.1.0"),
            Pair(">1.1", "1.1.1"),
            Pair(">=1.1", "1.0.2"),
            Pair(">=1.1", "0.0.9"),
            Pair(">=0", "0.0.0-alpha"),
            Pair(">=0.0", "0.0.0-alpha"),
            Pair("<0", "0.0.0"),
            Pair("=0", "1.0.0"),
            Pair("2.*", "3.0.0"),
            Pair("2", "2.0.0-alpha"),
            Pair("2.1.*", "2.2.1"),
            Pair("2", "3.0.0"),
            Pair("2.1", "2.2.1"),
            Pair("~1.2.3", "1.3.0"),
            Pair("~1.2", "1.3.0"),
            Pair("~1", "2.0.0"),
            Pair("~0.1.2", "0.2.0"),
            Pair("~0.0.1", "0.1.0-alpha"),
            Pair("~0.0.1", "0.1.0"),
            Pair("~2.4", "2.5.0"),
            Pair("~2.4", "2.3.9"),
            Pair("~>3.2.1", "3.3.2"),
            Pair("~>3.2.1", "3.2.0"),
            Pair("~1", "0.2.3"),
            Pair("~>1", "2.2.3"),
            Pair("~1.0", "1.1.0"),
        )

        data.forEach {
            assertFalse(SemanticVersion.parse(it.second) satisfies SemanticVersionConstraint.parse(it.first))
        }
    }

    @Test
    fun testParse() {
        val data: List<Pair<String, String>> = listOf(
            Pair("1.2.3 - 2.3.4", ">=1.2.3 <=2.3.4"),
            Pair("1.2.3 - 2.3.4 || 3.0.0 - 4.0.0", ">=1.2.3 <=2.3.4 || >=3.0.0 <=4.0.0"),
            Pair("1.2 - 2.3.4", ">=1.2.0 <=2.3.4"),
            Pair("1.2.3 - 2.3", ">=1.2.3 <2.4.0-0"),
            Pair("1.2.3 - 2", ">=1.2.3 <3.0.0-0"),
            Pair("~1.2.3", ">=1.2.3 <1.3.0-0"),
            Pair("~1.2", ">=1.2.0 <1.3.0-0"),
            Pair("~1", ">=1.0.0 <2.0.0-0"),
            Pair("~0.2.3", ">=0.2.3 <0.3.0-0"),
            Pair("~0.2", ">=0.2.0 <0.3.0-0"),
            Pair("~0", ">=0.0.0 <1.0.0-0"),
            Pair("~0.0.0", ">=0.0.0 <0.1.0-0"),
            Pair("~0.0", ">=0.0.0 <0.1.0-0"),
            Pair("~1.2.3-alpha.1", ">=1.2.3-alpha.1 <1.3.0-0"),
            Pair("", ">=0.0.0"),
            Pair("*", ">=0.0.0"),
            Pair("x", ">=0.0.0"),
            Pair("X", ">=0.0.0"),
            Pair("1.x", ">=1.0.0 <2.0.0-0"),
            Pair("1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("1", ">=1.0.0 <2.0.0-0"),
            Pair("1.*", ">=1.0.0 <2.0.0-0"),
            Pair("1.*.*", ">=1.0.0 <2.0.0-0"),
            Pair("1.x", ">=1.0.0 <2.0.0-0"),
            Pair("1.x.x", ">=1.0.0 <2.0.0-0"),
            Pair("1.X", ">=1.0.0 <2.0.0-0"),
            Pair("1.X.X", ">=1.0.0 <2.0.0-0"),
            Pair("1.2", ">=1.2.0 <1.3.0-0"),
            Pair("1.2.*", ">=1.2.0 <1.3.0-0"),
            Pair("1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("1.2.X", ">=1.2.0 <1.3.0-0"),
            Pair("^1.2.3", ">=1.2.3 <2.0.0-0"),
            Pair("^0.2.3", ">=0.2.3 <0.3.0-0"),
            Pair("^0.0.3", ">=0.0.3 <0.0.4-0"),
            Pair("^0", ">=0.0.0 <1.0.0-0"),
            Pair("^0.0", ">=0.0.0 <0.1.0-0"),
            Pair("^0.0.0", ">=0.0.0 <0.0.1-0"),
            Pair("^1.2.3-alpha.1", ">=1.2.3-alpha.1 <2.0.0-0"),
            Pair("^0.0.1-alpha", ">=0.0.1-alpha <0.0.2-0"),
            Pair("^0.0.*", ">=0.0.0 <0.1.0-0"),
            Pair("^1.2.*", ">=1.2.0 <2.0.0-0"),
            Pair("^1.*", ">=1.0.0 <2.0.0-0"),
            Pair("^0.*", ">=0.0.0 <1.0.0-0"),
            Pair("1.0.0 - 2.0.0", ">=1.0.0 <=2.0.0"),
            Pair("1 - 2", ">=1.0.0 <3.0.0-0"),
            Pair("1.0 - 2.0", ">=1.0.0 <2.1.0-0"),
            Pair("1.0.0", "=1.0.0"),
            Pair(">=*", ">=0.0.0"),
            Pair(">=1.0.0", ">=1.0.0"),
            Pair(">1.0.0", ">1.0.0"),
            Pair("<=2.0.0", "<=2.0.0"),
            Pair("<=2.0.0", "<=2.0.0"),
            Pair("<2.0.0", "<2.0.0"),
            Pair("<\t2.0.0", "<2.0.0"),
            Pair("<= 2.0.0", "<=2.0.0"),
            Pair("<=  2.0.0", "<=2.0.0"),
            Pair("<    2.0.0", "<2.0.0"),
            Pair("<    2.0", "<2.0.0"),
            Pair("<=    2.0", "<2.1.0-0"),
            Pair(">= 1.0.0", ">=1.0.0"),
            Pair(">=  1.0.0", ">=1.0.0"),
            Pair(">=   1.0.0", ">=1.0.0"),
            Pair("> 1.0.0", ">1.0.0"),
            Pair(">  1.0.0", ">1.0.0"),
            Pair("<=   2.0.0", "<=2.0.0"),
            Pair("0.1.0 || 1.2.3", "=0.1.0 || =1.2.3"),
            Pair(">=0.1.0 || <0.0.1", ">=0.1.0 || <0.0.1"),
            Pair("0.1.0 | 1.2.3", "=0.1.0 || =1.2.3"),
            Pair(">=0.1.0|<0.0.1", ">=0.1.0 || <0.0.1"),
            Pair("2.x.x", ">=2.0.0 <3.0.0-0"),
            Pair("1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("1.2.x || 2.x", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("1.2.x || 2.x", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("1.2.x|2.x", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("x", ">=0.0.0"),
            Pair("2.*.*", ">=2.0.0 <3.0.0-0"),
            Pair("1.2.*", ">=1.2.0 <1.3.0-0"),
            Pair("1.2.* || 2.*", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("1.2.*|2.*", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("*", ">=0.0.0"),
            Pair("2", ">=2.0.0 <3.0.0-0"),
            Pair("2.3", ">=2.3.0 <2.4.0-0"),
            Pair("~2.4", ">=2.4.0 <2.5.0-0"),
            Pair("~2.4", ">=2.4.0 <2.5.0-0"),
            Pair("~>3.2.1", ">=3.2.1 <3.3.0-0"),
            Pair("~1", ">=1.0.0 <2.0.0-0"),
            Pair("~>1", ">=1.0.0 <2.0.0-0"),
            Pair("~> 1", ">=1.0.0 <2.0.0-0"),
            Pair("~1.0", ">=1.0.0 <1.1.0-0"),
            Pair("~ 1.0", ">=1.0.0 <1.1.0-0"),
            Pair("^0", ">=0.0.0 <1.0.0-0"),
            Pair("^ 1", ">=1.0.0 <2.0.0-0"),
            Pair("^0.1", ">=0.1.0 <0.2.0-0"),
            Pair("^1.0", ">=1.0.0 <2.0.0-0"),
            Pair("^1.2", ">=1.2.0 <2.0.0-0"),
            Pair("^0.0.1", ">=0.0.1 <0.0.2-0"),
            Pair("^0.0.1-beta", ">=0.0.1-beta <0.0.2-0"),
            Pair("^0.1.2", ">=0.1.2 <0.2.0-0"),
            Pair("^1.2.3", ">=1.2.3 <2.0.0-0"),
            Pair("^1.2.3-beta.4", ">=1.2.3-beta.4 <2.0.0-0"),
            Pair("<1", "<1.0.0"),
            Pair("< 1", "<1.0.0"),
            Pair("= 1", ">=1.0.0 <2.0.0-0"),
            Pair("!= 1", "<1.0.0 || >=2.0.0-0"),
            Pair(">=1", ">=1.0.0"),
            Pair(">= 1", ">=1.0.0"),
            Pair("<1.2", "<1.2.0"),
            Pair("< 1.2", "<1.2.0"),
            Pair("1", ">=1.0.0 <2.0.0-0"),
            Pair("^ 1.2 ^ 1", ">=1.2.0 <2.0.0-0 >=1.0.0 <2.0.0-0"),
            Pair("1.2 - 3.4.5", ">=1.2.0 <=3.4.5"),
            Pair("1.2.3 - 3.4", ">=1.2.3 <3.5.0-0"),
            Pair("1.2 - 3.4", ">=1.2.0 <3.5.0-0"),
            Pair(">1", ">=2.0.0-0"),
            Pair(">1.2", ">=1.3.0-0"),
            Pair("<*", "<0.0.0-0"),
            Pair(">*", "<0.0.0-0"),
            Pair("!=*", "<0.0.0-0"),
            Pair(">=*", ">=0.0.0"),
            Pair("<=*", ">=0.0.0"),
            Pair("=*", ">=0.0.0"),

            Pair("v1.2.3 - v2.3.4", ">=1.2.3 <=2.3.4"),
            Pair("v1.2.3 - v2.3.4 || 3.0.0 - 4.0.0", ">=1.2.3 <=2.3.4 || >=3.0.0 <=4.0.0"),
            Pair("v1.2 - v2.3.4", ">=1.2.0 <=2.3.4"),
            Pair("v1.2.3 - v2.3", ">=1.2.3 <2.4.0-0"),
            Pair("v1.2.3 - v2", ">=1.2.3 <3.0.0-0"),
            Pair("~v1.2.3", ">=1.2.3 <1.3.0-0"),
            Pair("~v1.2", ">=1.2.0 <1.3.0-0"),
            Pair("~v1", ">=1.0.0 <2.0.0-0"),
            Pair("~v0.2.3", ">=0.2.3 <0.3.0-0"),
            Pair("~v0.2", ">=0.2.0 <0.3.0-0"),
            Pair("~v0", ">=0.0.0 <1.0.0-0"),
            Pair("~v0.0.0", ">=0.0.0 <0.1.0-0"),
            Pair("~v0.0", ">=0.0.0 <0.1.0-0"),
            Pair("~v1.2.3-alpha.1", ">=1.2.3-alpha.1 <1.3.0-0"),
            Pair("v*", ">=0.0.0"),
            Pair("vx", ">=0.0.0"),
            Pair("vX", ">=0.0.0"),
            Pair("v1.x", ">=1.0.0 <2.0.0-0"),
            Pair("v1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("v1", ">=1.0.0 <2.0.0-0"),
            Pair("v1.*", ">=1.0.0 <2.0.0-0"),
            Pair("v1.*.*", ">=1.0.0 <2.0.0-0"),
            Pair("v1.x", ">=1.0.0 <2.0.0-0"),
            Pair("v1.x.x", ">=1.0.0 <2.0.0-0"),
            Pair("v1.X", ">=1.0.0 <2.0.0-0"),
            Pair("v1.X.X", ">=1.0.0 <2.0.0-0"),
            Pair("v1.2", ">=1.2.0 <1.3.0-0"),
            Pair("v1.2.*", ">=1.2.0 <1.3.0-0"),
            Pair("v1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("v1.2.X", ">=1.2.0 <1.3.0-0"),
            Pair("^v1.2.3", ">=1.2.3 <2.0.0-0"),
            Pair("^v0.2.3", ">=0.2.3 <0.3.0-0"),
            Pair("^v0.0.3", ">=0.0.3 <0.0.4-0"),
            Pair("^v0", ">=0.0.0 <1.0.0-0"),
            Pair("^v0.0", ">=0.0.0 <0.1.0-0"),
            Pair("^v0.0.0", ">=0.0.0 <0.0.1-0"),
            Pair("^v1.2.3-alpha.1", ">=1.2.3-alpha.1 <2.0.0-0"),
            Pair("^v0.0.1-alpha", ">=0.0.1-alpha <0.0.2-0"),
            Pair("^v0.0.*", ">=0.0.0 <0.1.0-0"),
            Pair("^v1.2.*", ">=1.2.0 <2.0.0-0"),
            Pair("^v1.*", ">=1.0.0 <2.0.0-0"),
            Pair("^v0.*", ">=0.0.0 <1.0.0-0"),
            Pair("v1.0.0 - 2.0.0", ">=1.0.0 <=2.0.0"),
            Pair("v1 - v2", ">=1.0.0 <3.0.0-0"),
            Pair("v1.0 - v2.0", ">=1.0.0 <2.1.0-0"),
            Pair("v1.0.0", "=1.0.0"),
            Pair(">=v*", ">=0.0.0"),
            Pair(">=v1.0.0", ">=1.0.0"),
            Pair(">v1.0.0", ">1.0.0"),
            Pair("<=v2.0.0", "<=2.0.0"),
            Pair("<=v2.0.0", "<=2.0.0"),
            Pair("<v2.0.0", "<2.0.0"),
            Pair("<\tv2.0.0", "<2.0.0"),
            Pair("<= v2.0.0", "<=2.0.0"),
            Pair("<=  v2.0.0", "<=2.0.0"),
            Pair("<    v2.0.0", "<2.0.0"),
            Pair("<    v2.0", "<2.0.0"),
            Pair("<=    v2.0", "<2.1.0-0"),
            Pair(">= v1.0.0", ">=1.0.0"),
            Pair(">=  v1.0.0", ">=1.0.0"),
            Pair(">=   v1.0.0", ">=1.0.0"),
            Pair("> v1.0.0", ">1.0.0"),
            Pair(">  v1.0.0", ">1.0.0"),
            Pair("<=   v2.0.0", "<=2.0.0"),
            Pair("v0.1.0 || v1.2.3", "=0.1.0 || =1.2.3"),
            Pair(">=v0.1.0 || <v0.0.1", ">=0.1.0 || <0.0.1"),
            Pair("v2.x.x", ">=2.0.0 <3.0.0-0"),
            Pair("v1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("v1.2.x || v2.x", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("v1.2.x || v2.x", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("vx", ">=0.0.0"),
            Pair("v2.*.*", ">=2.0.0 <3.0.0-0"),
            Pair("v1.2.*", ">=1.2.0 <1.3.0-0"),
            Pair("v1.2.* || 2.*", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("v*", ">=0.0.0"),
            Pair("v2", ">=2.0.0 <3.0.0-0"),
            Pair("v2.3", ">=2.3.0 <2.4.0-0"),
            Pair("~v2.4", ">=2.4.0 <2.5.0-0"),
            Pair("~v2.4", ">=2.4.0 <2.5.0-0"),
            Pair("~>v3.2.1", ">=3.2.1 <3.3.0-0"),
            Pair("~v1", ">=1.0.0 <2.0.0-0"),
            Pair("~>v1", ">=1.0.0 <2.0.0-0"),
            Pair("~> v1", ">=1.0.0 <2.0.0-0"),
            Pair("~v1.0", ">=1.0.0 <1.1.0-0"),
            Pair("~ v1.0", ">=1.0.0 <1.1.0-0"),
            Pair("^v0", ">=0.0.0 <1.0.0-0"),
            Pair("^ v1", ">=1.0.0 <2.0.0-0"),
            Pair("^v0.1", ">=0.1.0 <0.2.0-0"),
            Pair("^v1.0", ">=1.0.0 <2.0.0-0"),
            Pair("^v1.2", ">=1.2.0 <2.0.0-0"),
            Pair("^v0.0.1", ">=0.0.1 <0.0.2-0"),
            Pair("^v0.0.1-beta", ">=0.0.1-beta <0.0.2-0"),
            Pair("^v0.1.2", ">=0.1.2 <0.2.0-0"),
            Pair("^v1.2.3", ">=1.2.3 <2.0.0-0"),
            Pair("^v1.2.3-beta.4", ">=1.2.3-beta.4 <2.0.0-0"),
            Pair("<v1", "<1.0.0"),
            Pair("< v1", "<1.0.0"),
            Pair("= v1", ">=1.0.0 <2.0.0-0"),
            Pair("!= v1", "<1.0.0 || >=2.0.0-0"),
            Pair(">=v1", ">=1.0.0"),
            Pair(">= v1", ">=1.0.0"),
            Pair("<v1.2", "<1.2.0"),
            Pair("< v1.2", "<1.2.0"),
            Pair("v1", ">=1.0.0 <2.0.0-0"),
            Pair("^ v1.2 ^ v1", ">=1.2.0 <2.0.0-0 >=1.0.0 <2.0.0-0"),
            Pair("v1.2 - v3.4.5", ">=1.2.0 <=3.4.5"),
            Pair("v1.2.3 - v3.4", ">=1.2.3 <3.5.0-0"),
            Pair("v1.2 - v3.4", ">=1.2.0 <3.5.0-0"),
            Pair(">v1", ">=2.0.0-0"),
            Pair(">v1.2", ">=1.3.0-0"),
            Pair("<v*", "<0.0.0-0"),
            Pair(">v*", "<0.0.0-0"),
            Pair("!=v*", "<0.0.0-0"),
            Pair(">=v*", ">=0.0.0"),
            Pair("<=v*", ">=0.0.0"),
            Pair("=v*", ">=0.0.0"),
            Pair("^7|^8", ">=7.0.0 <8.0.0-0 || >=8.0.0 <9.0.0-0"),
        )

        data.forEach {
            assertEquals(it.second, SemanticVersionConstraint.parse(it.first).toString())
        }
    }

}