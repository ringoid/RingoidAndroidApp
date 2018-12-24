package com.orcchg.githubuser.domain.model

interface Mappable<out R> {

    fun map(): R
}

interface TMappable<in T, out R> {

    fun map(model: T): R
}

interface ListMappable<in T, out R> where T : Mappable<R> {

    fun mapList(input: List<T>): List<R> = input.map { it.map() }
}

interface TListMappable<in T, out R> where T : TMappable<T, R> {

    fun mapList(input: List<T>): List<R> = input.map { it.map(it) }
}

fun <T, R> List<T>.mapList(): List<R> where T : Mappable<R> = this.map { it.map() }
