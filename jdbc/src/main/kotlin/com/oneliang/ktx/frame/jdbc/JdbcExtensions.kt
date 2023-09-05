package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants

private fun <T> sqlConditionTransform(instance: T): String {
    return if (instance is Boolean) {
        instance.toString()
    } else {
        Constants.Symbol.SINGLE_QUOTE + instance.toString() + Constants.Symbol.SINGLE_QUOTE
    }
}

fun <T> Iterable<T>.toSqlCondition(transform: (T) -> String = { it.toString() }): String {
    return this.joinToString {
        sqlConditionTransform(transform(it))
    }
}

fun <T> Array<T>.toSqlCondition(transform: (T) -> String = { it.toString() }): String {
    return this.joinToString {
        sqlConditionTransform(transform(it))
    }
}

fun String.toSqlEqual(value: String = Constants.Symbol.QUESTION_MARK): String {
    return "$this=$value"
}

fun String.toSqlNotEqual(value: String = Constants.Symbol.QUESTION_MARK): String {
    return "$this!=$value"
}

fun String.toSqlAndEqual(value: String = Constants.Symbol.QUESTION_MARK): String {
    return " ${Constants.Database.AND} $this=$value"
}

fun String.toSqlAndNotEqual(value: String = Constants.Symbol.QUESTION_MARK): String {
    return " ${Constants.Database.AND} $this!=$value"
}

fun String.toSqlOrEqual(value: String = Constants.Symbol.QUESTION_MARK): String {
    return " ${Constants.Database.OR} $this=$value"
}

fun String.toSqlOrNotEqual(value: String = Constants.Symbol.QUESTION_MARK): String {
    return " ${Constants.Database.OR} $this!=$value"
}

fun String.toSqlAndLike(value: String = Constants.Symbol.QUESTION_MARK): String {
    return " ${Constants.Database.AND} $this ${Constants.Database.LIKE} $value"
}

fun String.toSqlOrLike(value: String = Constants.Symbol.QUESTION_MARK): String {
    return " ${Constants.Database.OR} $this ${Constants.Database.LIKE} $value"
}

fun String.toSqlIn(subSql: String): String {
    return "$this ${Constants.Database.IN} ($subSql)"
}

fun String.toSqlAndIn(subSql: String): String {
    return " ${Constants.Database.AND} $this ${Constants.Database.IN} ($subSql)"
}

fun String.toSqlOrIn(subSql: String): String {
    return " ${Constants.Database.OR} $this ${Constants.Database.IN} ($subSql)"
}

fun String.toSqlAs(columnAlias: String): String {
    return "$this ${Constants.Database.AS} $columnAlias"
}

fun String.toSqlLeftJoin(subSql: String): String {
    return "$this ${Constants.Database.LEFT_JOIN} $subSql"
}

fun String.toSqlOnEqual(value: String): String {
    return " ${Constants.Database.ON} $this=$value"
}

fun String.toSqlOrderByDesc(includeOrderBy: Boolean = true): String {
    return if (includeOrderBy) {
        "${Constants.Database.ORDER_BY} $this ${Constants.Database.DESC}"
    } else {
        "$this ${Constants.Database.DESC}"
    }
}

fun String.toSqlOrderByAsc(includeOrderBy: Boolean = true): String {
    return if (includeOrderBy) {
        "${Constants.Database.ORDER_BY} $this ${Constants.Database.ASC}"
    } else {
        "$this ${Constants.Database.ASC}"
    }
}
