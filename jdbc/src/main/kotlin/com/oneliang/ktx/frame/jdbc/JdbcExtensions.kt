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
    return " AND $this=$value"
}

fun String.toSqlAndNotEqual(value: String = Constants.Symbol.QUESTION_MARK): String {
    return " AND $this!=$value"
}

fun String.toSqlOrEqual(value: String = Constants.Symbol.QUESTION_MARK): String {
    return " OR $this=$value"
}

fun String.toSqlOrNotEqual(value: String = Constants.Symbol.QUESTION_MARK): String {
    return " OR $this!=$value"
}

fun String.toSqlAndLike(value: String = Constants.Symbol.QUESTION_MARK): String {
    return " AND $this LIKE $value"
}

fun String.toSqlOrLike(value: String = Constants.Symbol.QUESTION_MARK): String {
    return " OR $this LIKE $value"
}

fun String.toSqlIn(subSql: String = Constants.String.BLANK): String {
    return "$this IN ($subSql)"
}

fun String.toSqlAndIn(subSql: String = Constants.String.BLANK): String {
    return " AND $this IN ($subSql)"
}

fun String.toSqlOrIn(subSql: String = Constants.String.BLANK): String {
    return " OR $this IN ($subSql)"
}

fun String.toSqlAs(columnAlias: String): String {
    return "$this AS $columnAlias"
}

fun String.toSqlLeftJoin(subSql: String): String {
    return "$this LEFT JOIN $subSql"
}

fun String.toSqlOnEqual(value: String): String {
    return " ON $this=$value"
}