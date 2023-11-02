package com.osmerion.kotlin.semver.constraints

import com.osmerion.kotlin.semver.Version

internal interface VersionComparator {
    fun isSatisfiedBy(version: Version): Boolean
    fun opposite(): String

    companion object {
        val greaterThanMin: VersionComparator = Condition(Op.GREATER_THAN_OR_EQUAL, Version.min)
    }
}
