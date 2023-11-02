package com.osmerion.kotlin.semver.constraints

import com.osmerion.kotlin.semver.SemanticVersion

internal interface VersionComparator {
    fun isSatisfiedBy(version: SemanticVersion): Boolean
    fun opposite(): String

    companion object {
        val greaterThanMin: VersionComparator = Condition(Op.GREATER_THAN_OR_EQUAL, SemanticVersion.min)
    }
}
