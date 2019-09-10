package com.ringoid.utility.test

import org.junit.Assert
import org.junit.Test

class SyntaxOperatorTest {

    data class Model(val name: String)

    private fun getNotNull1(): Model? = Model(name = "Sky")
    private fun getNotNull2(): Model? = Model(name = "Cloud")
    private fun getNotNull3(): Model? = Model(name = "Ground")
    private fun getNull(): Model? = null

    @Test
    fun testMultipleElvisOperators_variousUseCases_lastValueShouldBeDefinedProperly() {
        val data1 = getNotNull1()
            ?: getNotNull2()
            ?: getNotNull3()

        Assert.assertEquals(getNotNull1(), data1)

        val data2 = getNull()
            ?: getNotNull2()
            ?: getNotNull3()

        Assert.assertEquals(getNotNull2(), data2)

        val data3 = getNull()
            ?: getNull()
            ?: getNotNull3()

        Assert.assertEquals(getNotNull3(), data3)

        val data4 = getNull()
            ?: getNull()
            ?: getNull()

        Assert.assertTrue(data4 == null)
    }
}
