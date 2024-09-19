/*
 * SPDX-FileCopyrightText: 2023-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.query

typealias Column = String

sealed interface Node {
    fun build(): String = when (this) {
        is And -> "(${lhs.build()}) AND (${rhs.build()})"
        is Eq -> "${lhs.build()} = ${rhs.build()}"
        is Neq -> "${lhs.build()} != ${rhs.build()}"
        is In<*> -> "$value IN (${values.joinToString(", ")})"
        is Like -> "${lhs.build()} LIKE ${rhs.build()}"
        is Literal<*> -> "$`val`"
        is Or -> "(${lhs.build()}) OR (${rhs.build()})"
    }
}

private class And(val lhs: Node, val rhs: Node) : Node
private class Eq(val lhs: Node, val rhs: Node) : Node
private class Neq(val lhs: Node, val rhs: Node) : Node
private class In<T>(val value: T, val values: Collection<T>) : Node
private class Like(val lhs: Node, val rhs: Node) : Node
private class Literal<T>(val `val`: T) : Node
private class Or(val lhs: Node, val rhs: Node) : Node

class Query(val root: Node) {
    fun build() = root.build()

    companion object {
        const val ARG = "?"
    }
}

infix fun Query.and(other: Query) = Query(And(this.root, other.root))
infix fun Query.eq(other: Query) = Query(Eq(this.root, other.root))
infix fun Query.neq(other: Query) = Query(Neq(this.root, other.root))
infix fun Query.like(other: Query) = Query(Like(this.root, other.root))
infix fun Query.or(other: Query) = Query(Or(this.root, other.root))

infix fun <T> Column.eq(other: T) = Query(Literal(this)) eq Query(Literal(other))
infix fun <T> Column.neq(other: T) = Query(Literal(this)) neq Query(Literal(other))
infix fun <T> Column.`in`(values: Collection<T>) = Query(In(this, values))
infix fun <T> Column.like(other: T) = Query(Literal(this)) like Query(Literal(other))

fun Iterable<Query>.join(
    func: Query.(other: Query) -> Query,
) = reduce(func)

fun Iterable<Query>.joinNullable(
    func: Query.(other: Query) -> Query,
) = reduceOrNull(func)
