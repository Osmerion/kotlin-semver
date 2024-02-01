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
@file:OptIn(ExperimentalConstraintApi::class)
package com.osmerion.kotlin.semver.constraints.npm.internal

import com.osmerion.kotlin.semver.constraints.ExperimentalConstraintApi
import com.osmerion.kotlin.semver.constraints.VersionPredicate
import com.osmerion.kotlin.semver.internal.PreRelease

internal data class Options(val loose: Boolean, val includePrerelease: Boolean)

internal fun parseRange(source: CharSequence, options: Options): List<VersionPredicate> {
    var source = SemVerPatterns.HYPHEN_RANGE.replace(source) { m ->
        if (tryParseNpmVersionDescriptor(m.value) != null) {
            m.value
        } else {
            "${m.groupValues[1]} - ${m.groupValues[9]}"
        }
    }

    // Comparator Trim: `> 1.2.3 < 1.2.5` => `>1.2.3 <1.2.5`
    source = SemVerPatterns.COMPARATOR_TRIM.replace(source) { m ->
        "${m.groupValues[1]}${m.groupValues[2]}${m.groupValues[3]}"
    }

    // Tilde Trim: `~ 1.2.3` => `~1.2.3`
    source = SemVerPatterns.TILDE_TRIM.replace(source) { m ->
        "~${m.groupValues[1]}"
    }

    // Caret Trim: `^ 1.2.3` => `^1.2.3`
    source = SemVerPatterns.CARET_TRIM.replace(source) { m ->
        "^${m.groupValues[1]}"
    }

    // Prefix Trim: `v 1.2.3` => `v1.2.3`
    source = SemVerPatterns.X_RANGE_LOOSE.replace(source) { m ->
        "${m.groupValues[1].replace("\\s*".toRegex(), "")}${m.groupValues[2]}"
    }

    val predicates = source.split("(?<!-)\\s+(?!-)".toRegex())
        .map { comp -> parsePredicate(comp, options) }

    return predicates
}

private fun parsePredicate(comp: String, options: Options): VersionPredicate {
    val comp = comp.replace(SemVerPatterns.BUILD, "").trim()

    val descriptor = tryParseNpmVersionDescriptor(comp)
    if (descriptor != null) return XVersionRange(descriptor = descriptor)

    hyphenReplace(comp, options)?.let { return it }

    val op = comp.startsWithOperator()

    return when {
        op != null -> ComparatorPredicate(
            descriptor = parseNpmVersionDescriptor(comp.substring(startIndex = op.toString().length)),
            op = op
        )
        isX(comp) -> AnyVersion(comp)
        comp.startsWith("^") -> parseCaret(comp)
        comp.startsWith("~") -> parseTilde(comp)
        else -> {
            val descriptor = parseNpmVersionDescriptor(comp)
            XVersionRange(descriptor = descriptor)
        }
    }
}

private fun parseTilde(comp: String): TildeVersionRange {
    val (match) = SemVerPatterns.TILDE.matchEntire(comp)?.destructured ?: TODO()
    return TildeVersionRange(parseNpmVersionDescriptor(match))
}

private fun parseCaret(comp: String): CaretVersionRange {
    val (match) = SemVerPatterns.CARET.matchEntire(comp)?.destructured ?: TODO()
    return CaretVersionRange(parseNpmVersionDescriptor(match))
}

private fun hyphenReplace(source: CharSequence, options: Options): VersionPredicate? {
    val m = (if (options.loose) SemVerPatterns.HYPHEN_RANGE_LOOSE else SemVerPatterns.HYPHEN_RANGE).matchEntire(source) ?: return null

    // Groups:
    // 1=from, 2=fM, 3=fm, 4=fp, 5=fpr, 6=fb
    // 7=to,   8=tM, 9=tm, 10=tp, 11=tpr

    // Note: The capture groups logic depends heavily on the exact regex structure.
    // Assuming standard SemVer regex structure where a full version has ~5 groups.

    val fM = m.groupValues[4]
    val fm = m.groupValues[5]
    val fp = m.groupValues[6]
    val fpr = m.groupValues[7].let { if (it.isNotBlank()) PreRelease(it) else null }

    // fb (6) ignored

    val tM = m.groupValues[12]
    val tm = m.groupValues[13]
    val tp = m.groupValues[14]
    val tpr = m.groupValues[15].let { if (it.isNotBlank()) PreRelease(it) else null }

//    if (isX(fM)) from = ""
//    else if (isX(fm)) from = ">=$fM.0.0${if (incPr) "-0" else ""}"
//    else if (isX(fp)) from = ">=$fM.$fm.0${if (incPr) "-0" else ""}"
//    else if (fpr.isNotEmpty()) from = ">=$from"
//    else from = ">=$from${if (incPr) "-0" else ""}"
//
//    if (isX(tM)) to = ""
//    else if (isX(tm)) to = "<${tM.toInt() + 1}.0.0-0"
//    else if (isX(tp)) to = "<$tM.${tm.toInt() + 1}.0-0"
//    else if (tpr.isNotEmpty()) to = "<=$tM.$tm.$tp-$tpr"
//    else if (incPr) to = "<$tM.$tm.${tp.toInt() + 1}-0"
//    else to = "<=$to"

    return HyphenVersionRange(
        lowerBound = RegularNpmVersionDescriptor(fM, fm, fp, fpr),
        upperBound = RegularNpmVersionDescriptor(tM, tm, tp, tpr)
    )
}

// -- Utilities --

internal fun isX(id: String?): Boolean = id == null || id == "" || id.lowercase() == "x" || id == "*"

/**
 * Regex Definitions.
 * Reconstructed based on standard SemVer patterns used in node-semver.
 */
internal object SemVerPatterns {

    private val LETTERDASHNUMBER = "[a-zA-Z0-9-]".toRegex()

    // ## Numeric Identifier
    // A single `0`, or a non-zero digit followed by zero or more digits.

    private val NUMERIC_IDENTIFIER = "0|[1-9]\\d*".toRegex()
    private val NUMERIC_IDENTIFIER_LOOSE = "\\d+".toRegex()

    // ## Non-numeric Identifier
    // Zero or more digits, followed by a letter or hyphen, and then zero or
    // more letters, digits, or hyphens.

    private val NONNUMERIC_IDENTIFIER = "\\d*[a-zA-Z-]$LETTERDASHNUMBER*".toRegex()

    // ## Main Version
    // Three dot-separated numeric identifiers.

    private val MAIN_VERSION = "($NUMERIC_IDENTIFIER)\\.($NUMERIC_IDENTIFIER)\\.($NUMERIC_IDENTIFIER)".toRegex()
    private val MAIN_VERSION_LOOSE = "($NUMERIC_IDENTIFIER_LOOSE)\\.($NUMERIC_IDENTIFIER_LOOSE)\\.($NUMERIC_IDENTIFIER_LOOSE)".toRegex()

    // ## Pre-release Version Identifier
    // A numeric identifier, or a non-numeric identifier.
    // Non-numeric identifiers include numeric identifiers but can be longer.
    // Therefore non-numeric identifiers must go first.

    private val PRERELEASE_IDENTIFIER = "(?:$NONNUMERIC_IDENTIFIER|$NUMERIC_IDENTIFIER)".toRegex()
    private val PRERELEASE_IDENTIFIER_LOOSE = "(?:$NONNUMERIC_IDENTIFIER|$NUMERIC_IDENTIFIER_LOOSE)".toRegex()

    // ## Pre-release Version
    // Hyphen, followed by one or more dot-separated pre-release version
    // identifiers.

    private val PRERELEASE = "(?:-($PRERELEASE_IDENTIFIER(?:\\.$PRERELEASE_IDENTIFIER)*))".toRegex()
    private val PRERELEASE_LOOSE = "(?:-($PRERELEASE_IDENTIFIER_LOOSE(?:\\.$PRERELEASE_IDENTIFIER_LOOSE)*))".toRegex()

    // ## Build Metadata Identifier
    // Any combination of digits, letters, or hyphens.

    private val BUILD_IDENTIFIER = "$LETTERDASHNUMBER+".toRegex()

    // ## Build Metadata
    // Plus sign, followed by one or more period-separated build metadata
    // identifiers.

    val BUILD = "(?:\\+($BUILD_IDENTIFIER(?:\\.$BUILD_IDENTIFIER)*))".toRegex()

    // ## Full Version String
    // A main version, followed optionally by a pre-release version and
    // build metadata.

    // Note that the only major, minor, patch, and pre-release sections of
    // the version string are capturing groups.  The build metadata is not a
    // capturing group, because it should not ever be used in version
    // comparison.

    private val FULL_PLAIN = "v?$MAIN_VERSION$PRERELEASE?$BUILD?".toRegex()

    private val FULL = "^$FULL_PLAIN$".toRegex()

    private val LOOSE_PLAIN = "([v=\\s]*)$MAIN_VERSION_LOOSE$PRERELEASE_LOOSE?$BUILD?".toRegex()

    private val LOOSE = "^$LOOSE_PLAIN$".toRegex()

    private val GTLT = "([<>]?=?)".toRegex()

    private val X_RANGE_IDENTIFIER = "$NUMERIC_IDENTIFIER|x|X|\\*".toRegex()
    private val X_RANGE_IDENTIFIER_LOOSE = "$NUMERIC_IDENTIFIER_LOOSE|x|X|\\*".toRegex()

    private val X_RANGE_PLAIN = "([v=\\s]*)(($X_RANGE_IDENTIFIER)(?:\\.($X_RANGE_IDENTIFIER)(?:\\.($X_RANGE_IDENTIFIER)(?:$PRERELEASE)?$BUILD?)?)?)".toRegex()
    private val X_RANGE_PLAIN_LOOSE = "([v=\\s]*)(($X_RANGE_IDENTIFIER_LOOSE)(?:\\.($X_RANGE_IDENTIFIER_LOOSE)(?:\\.($X_RANGE_IDENTIFIER_LOOSE)(?:$PRERELEASE)?$BUILD?)?)?)".toRegex()

    val X_RANGE = "^$X_RANGE_PLAIN$".toRegex()
    val X_RANGE_LOOSE = "^$X_RANGE_PLAIN_LOOSE$".toRegex()

    // Coercion.
    // Extract anything that could conceivably be a part of a valid semver

    val COERCEPLAIN = ""

//        createToken('COERCEPLAIN', `${'(^|[^\\d])' +
//            '(\\d{1,'}${MAX_SAFE_COMPONENT_LENGTH}})` +
//    `(?:\\.(\\d{1,${MAX_SAFE_COMPONENT_LENGTH}}))?` +
//    `(?:\\.(\\d{1,${MAX_SAFE_COMPONENT_LENGTH}}))?`)
//    createToken('COERCE', `${src[t.COERCEPLAIN]}(?:$|[^\\d])`)
//    createToken('COERCEFULL', src[t.COERCEPLAIN] +
//    `(?:${src[t.PRERELEASE]})?` +
//    `(?:${src[t.BUILD]})?` +
//    `(?:$|[^\\d])`)
//    createToken('COERCERTL', src[t.COERCE], true)
//    createToken('COERCERTLFULL', src[t.COERCEFULL], true)

    // Tilde ranges.
    // Meaning is "reasonably at or greater than"
    private val LONETILDE = "(?:~>?)".toRegex()

    val TILDE_TRIM = "(\\s*)$LONETILDE\\s+".toRegex()
//    exports.tildeTrimReplace = '$1~'

    val TILDE = "^$LONETILDE($X_RANGE_PLAIN)$".toRegex()

    // Caret ranges.
    // Meaning is "at least and backwards compatible with"
    private val LONECARET = "(?:\\^)".toRegex()

    val CARET_TRIM = "(\\s*)$LONECARET\\s+".toRegex()
//    exports.caretTrimReplace = '$1^'

    val CARET = "^$LONECARET$X_RANGE_PLAIN$".toRegex()

    // A simple gt/lt/eq thing, or just "" to indicate "any version"
    private val COMPARATOR = "^$GTLT\\s*($FULL_PLAIN)$|\\^$".toRegex()

    // An expression to strip any whitespace between the gtlt and the thing
    // it modifies, so that `> 1.2.3` ==> `>1.2.3`
    val COMPARATOR_TRIM = "(\\s*)$GTLT\\s*($LOOSE_PLAIN|$X_RANGE_PLAIN)".toRegex()
//    exports.comparatorTrimReplace = '$1$2$3'

    // Something like `1.2.3 - 1.2.4`
    // Note that these all use the loose form, because they'll be
    // checked against either the strict or loose comparator form
    // later.
    val HYPHEN_RANGE = "^\\s*($X_RANGE_PLAIN)\\s*-\\s*($X_RANGE_PLAIN)\\s*$".toRegex()
    val HYPHEN_RANGE_LOOSE = "^\\s*($X_RANGE_PLAIN_LOOSE)\\s*-\\s*($X_RANGE_PLAIN_LOOSE)\\s*$".toRegex()

    // Star ranges basically just allow anything at all.
    val STAR = "([<>])?=?\\s*\\*".toRegex()

    // >=0.0.0 is like a star
    private val GTE0 = "^\\s*>=\\s*0\\.0\\.0\\s*$".toRegex()


//createToken('GTE0PRE', '^\\s*>=\\s*0\\.0\\.0-0\\s*$')

}
