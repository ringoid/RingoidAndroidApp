package com.ringoid.utility.test

import com.ringoid.utility.randomString
import org.junit.Assert
import org.junit.Test

class CollectionTest {

    data class Model(val id: String, val payload: String = randomString())

    @Test
    fun twoPagesOfIds_allItemsUnique_secondPageNotChanged() {
        val pageOne = listOf(Model("1"), Model("2"), Model("3"), Model("4"), Model("5"))
        val pageTwo = listOf(Model("0"), Model("9"), Model("7"), Model("8"), Model("6"))

        val distinctIds = mutableSetOf<String>()
        distinctIds.addAll(pageOne.map { it.id })

        val distinctIdsOnPage = pageTwo.map { it.id }.minus(distinctIds)
        val result = if (distinctIdsOnPage.size != pageTwo.size) {
            // there are duplicates detected on this page with all the previous pages
            pageTwo.toMutableList().apply { retainAll { it.id in distinctIdsOnPage } }
        } else pageTwo

        val concat = mutableListOf<Model>().apply {
            addAll(pageOne)
            addAll(pageTwo)
        }
        val concatResult = mutableListOf<Model>().apply {
            addAll(pageOne)
            addAll(result)
        }

        Assert.assertArrayEquals(result.toTypedArray(), pageTwo.toTypedArray())
        Assert.assertArrayEquals(concat.toTypedArray(), concatResult.toTypedArray())
    }

    @Test
    fun twoPagesOfIds_someItemsRepeat_secondPageHasRetainedOnlyNewItems() {
        val retModel1 = Model("0")
        val retModel2 = Model("7")
        val retModel3 = Model("6")
        val pageOne = listOf(Model("1"), Model("2"), Model("3"), Model("4"), Model("5"))
        val pageTwo = listOf(retModel1, Model("2"), retModel2, Model("5"), retModel3)
        val retained = listOf(retModel1, retModel2, retModel3)

        val distinctIds = mutableSetOf<String>()
        distinctIds.addAll(pageOne.map { it.id })

        val distinctIdsOnPage = pageTwo.map { it.id }.minus(distinctIds)
        val result = if (distinctIdsOnPage.size != pageTwo.size) {
            // there are duplicates detected on this page with all the previous pages
            pageTwo.toMutableList().apply { retainAll { it.id in distinctIdsOnPage } }
        } else pageTwo

        val concat = mutableListOf<Model>().apply {
            addAll(pageOne)
            addAll(pageTwo)
        }
        val concatResult = mutableListOf<Model>().apply {
            addAll(pageOne)
            addAll(result)
        }

        Assert.assertArrayEquals(result.toTypedArray(), retained.toTypedArray())
        Assert.assertArrayEquals(concat.distinctBy { it.id }.toTypedArray(), concatResult.toTypedArray())
    }

    @Test
    fun twoPagesOfIds_secondPageIsEmpty_listContainsOnlyFirstPageItems() {
        val pageOne = listOf(Model("1"), Model("2"), Model("3"), Model("4"), Model("5"))
        val pageTwo = emptyList<Model>()

        val distinctIds = mutableSetOf<String>()
        distinctIds.addAll(pageOne.map { it.id })

        val distinctIdsOnPage = pageTwo.map { it.id }.minus(distinctIds)
        val result = if (distinctIdsOnPage.size != pageTwo.size) {
            // there are duplicates detected on this page with all the previous pages
            pageTwo.toMutableList().apply { retainAll { it.id in distinctIdsOnPage } }
        } else pageTwo

        val concat = mutableListOf<Model>().apply {
            addAll(pageOne)
            addAll(pageTwo)
        }
        val concatResult = mutableListOf<Model>().apply {
            addAll(pageOne)
            addAll(result)
        }

        Assert.assertTrue(result.isEmpty())
        Assert.assertArrayEquals(concat.toTypedArray(), concatResult.toTypedArray())
    }

    @Test
    fun twoPagesOfIds_firstPageIsEmpty_listContainsOnlySecondPageItems() {
        val pageOne = emptyList<Model>()
        val pageTwo = listOf(Model("1"), Model("2"), Model("3"), Model("4"), Model("5"))

        val distinctIds = mutableSetOf<String>()
        distinctIds.addAll(pageOne.map { it.id })

        val distinctIdsOnPage = pageTwo.map { it.id }.minus(distinctIds)
        val result = if (distinctIdsOnPage.size != pageTwo.size) {
            // there are duplicates detected on this page with all the previous pages
            pageTwo.toMutableList().apply { retainAll { it.id in distinctIdsOnPage } }
        } else pageTwo

        val concat = mutableListOf<Model>().apply {
            addAll(pageOne)
            addAll(pageTwo)
        }
        val concatResult = mutableListOf<Model>().apply {
            addAll(pageOne)
            addAll(result)
        }

        Assert.assertArrayEquals(result.toTypedArray(), pageTwo.toTypedArray())
        Assert.assertArrayEquals(concat.toTypedArray(), concatResult.toTypedArray())
    }
}
