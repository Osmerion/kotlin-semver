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
package com.osmerion.kotlin.semver.internal

internal object Patterns {

    // Numeric identifier pattern. (used for parsing major, minor, and patch)
    private const val NUMERIC = "0|[1-9]\\d*"

    // Alphanumeric or hyphen pattern.
    private const val ALPHANUMERIC_OR_HYPHEN = "[0-9a-zA-Z-]"

    // Letter or hyphen pattern.
    private const val LETTER_OR_HYPHEN = "[a-zA-Z-]"

    // Non-numeric identifier pattern. (used for parsing pre-release)
    private const val NON_NUMERIC = "\\d*$LETTER_OR_HYPHEN$ALPHANUMERIC_OR_HYPHEN*"

    // Dot-separated numeric identifier pattern. (<major>.<minor>.<patch>)
    private const val CORE_VERSION = "($NUMERIC)\\.($NUMERIC)\\.($NUMERIC)"

    // Dot-separated loose numeric identifier pattern. (<major>(.<minor>)?(.<patch>)?)
    private const val LOOSE_CORE_VERSION = "($NUMERIC)(?:\\.($NUMERIC))?(?:\\.($NUMERIC))?"

    // Numeric or non-numeric pre-release part pattern.
    private const val PRE_RELEASE_PART = "(?:$NUMERIC|$NON_NUMERIC)"

    // Pre-release identifier pattern. A hyphen followed by dot-separated
    // numeric or non-numeric pre-release parts.
    private const val PRE_RELEASE = "(?:-($PRE_RELEASE_PART(?:\\.$PRE_RELEASE_PART)*))"

    // Build-metadata identifier pattern. A + sign followed by dot-separated
    // alphanumeric build-metadata parts.
    private const val BUILD = "(?:\\+($ALPHANUMERIC_OR_HYPHEN+(?:\\.$ALPHANUMERIC_OR_HYPHEN+)*))"

    // List of allowed operations in a condition.
    private const val ALLOWED_OPERATORS = "||=|!=|<|<=|=<|>|>=|=>|\\^|~>|~"

    // Numeric identifier pattern for parsing conditions.
    private const val X_RANGE_NUMERIC = "$NUMERIC|x|X|\\*"

    // X-RANGE version: 1.x | 1.2.* | 1.1.X
    private const val X_RANGE_VERSION = "($X_RANGE_NUMERIC)(?:\\.($X_RANGE_NUMERIC)(?:\\.($X_RANGE_NUMERIC)(?:$PRE_RELEASE)?$BUILD?)?)?"

    // Pattern that only matches numbers.
    internal const val ONLY_NUMBER_REGEX: String = "^[0-9]+$"

    // Pattern that only matches alphanumeric or hyphen characters.
    internal const val ONLY_ALPHANUMERIC_OR_HYPHEN_REGEX: String = "^$ALPHANUMERIC_OR_HYPHEN+$"

    // Version parsing pattern: 1.2.3-alpha+build
    internal const val VERSION_REGEX: String = "^$CORE_VERSION$PRE_RELEASE?$BUILD?\$"

    // Prefixed version parsing pattern: v1.2-alpha+build
    internal const val LOOSE_VERSION_REGEX: String = "^v?$LOOSE_CORE_VERSION$PRE_RELEASE?$BUILD?\$"

    // Operator condition: >=1.2.*
    internal const val OPERATOR_CONDITION_REGEX = "($ALLOWED_OPERATORS)\\s*v?(?:$X_RANGE_VERSION)"

    // Hyphen range condition: 1.2.* - 2.0.0
    internal const val HYPHEN_CONDITION_REGEX = "\\s*v?(?:$X_RANGE_VERSION)\\s+-\\s+v?(?:$X_RANGE_VERSION)\\s*"

}