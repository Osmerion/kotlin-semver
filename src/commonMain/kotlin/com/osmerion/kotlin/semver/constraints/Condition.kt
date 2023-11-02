package com.osmerion.kotlin.semver.constraints

import com.osmerion.kotlin.semver.SemanticVersion

internal class Condition(private val operator: Op, private val version: SemanticVersion) : VersionComparator {

    override fun isSatisfiedBy(version: SemanticVersion): Boolean {
        return when (operator) {
            Op.EQUAL -> version == this.version
            Op.NOT_EQUAL -> version != this.version
            Op.LESS_THAN -> version < this.version
            Op.LESS_THAN_OR_EQUAL -> version <= this.version
            Op.GREATER_THAN -> version > this.version
            Op.GREATER_THAN_OR_EQUAL -> version >= this.version
        }
    }

    override fun opposite(): String {
        return when (operator) {
            Op.EQUAL -> "${Op.NOT_EQUAL}$version"
            Op.NOT_EQUAL -> "${Op.EQUAL}$version"
            Op.LESS_THAN -> "${Op.GREATER_THAN_OR_EQUAL}$version"
            Op.LESS_THAN_OR_EQUAL -> "${Op.GREATER_THAN}$version"
            Op.GREATER_THAN -> "${Op.LESS_THAN_OR_EQUAL}$version"
            Op.GREATER_THAN_OR_EQUAL -> "${Op.LESS_THAN}$version"
        }
    }

    override fun toString(): String {
        return "$operator$version"
    }
}
