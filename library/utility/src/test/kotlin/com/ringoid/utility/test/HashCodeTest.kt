package com.ringoid.utility.test

import com.ringoid.utility.goodHashCode
import com.ringoid.utility.randomInt
import org.greenrobot.essentials.hash.Murmur3F
import org.junit.Assert
import org.junit.Test

class HashCodeTest {

    @Test
    fun calcHashCode_sameString_hashCodesMustBeEqual() {
        val id1 = "2bd92a2880820449502181b45687aad6e90a6132"
        val hash10 = id1.goodHashCode()
        val hash11 = id1.goodHashCode()
        Assert.assertEquals(hash10, hash11)
    }

    @Test
    fun calcHashCode_sameStringDifferentInstances_hashCodesMustBeEqual() {
        val id1 = "2bd92a2880820449502181b45687aad6e90a6132"
        val id2 = "2bd92a2880820449502181b45687aad6e90a6132"
        val hash10 = id1.goodHashCode()
        val hash20 = id2.goodHashCode()
        Assert.assertEquals(hash10, hash20)
    }

    @Test
    fun calcHashCode_differentStrings_hashCodesMustNotBeEqual() {
        val id1 = "2bd92a2880820449502181b45687aad6e90a6132"
        val id2 = "30b49531684f7de2685b044d13e1c94992ad7342"
        val hash10 = id1.goodHashCode()
        val hash20 = id2.goodHashCode()
        Assert.assertNotEquals(hash10, hash20)
    }

    @Test
    fun calcHashCode_sameStringReset_hashCodesMustBeEqual() {
        val murmur3F by lazy { Murmur3F(randomInt()) }
        val id1 = "2bd92a2880820449502181b45687aad6e90a6132"
        val hash10 = id1.goodHashCode(murmur3F)
        murmur3F.reset()
        val hash11 = id1.goodHashCode(murmur3F)
        Assert.assertEquals(hash10, hash11)
    }

    @Test
    fun calcHashCode_sameStringProgressiveUpdate_hashCodesMustBeEqual() {
        val id1 = "f51a19a345f0ffa691ae30d41275ef81fb1343c3"
        val id2 = "30b49531684f7de2685b044d13e1c94992ad7342"
        val hash10 = id1.goodHashCode()
        val hash20 = id2.goodHashCode()  // progressive update hash engine
        val hash11 = id1.goodHashCode()
        val hash21 = id2.goodHashCode()
        Assert.assertEquals(hash10, hash11)
        Assert.assertEquals(hash20, hash21)
        Assert.assertNotEquals(hash10, hash20)
        Assert.assertNotEquals(hash10, hash21)
        Assert.assertNotEquals(hash11, hash20)
        Assert.assertNotEquals(hash11, hash21)
    }

    @Test
    fun calcHashCode_sameStringResetBeforeUpdate_hashCodesMustBeEqual() {
        val murmur3F by lazy { Murmur3F(randomInt()) }
        val id1 = "f51a19a345f0ffa691ae30d41275ef81fb1343c3"
        val id2 = "30b49531684f7de2685b044d13e1c94992ad7342"
        val hash10 = id1.goodHashCode(murmur3F)
        murmur3F.reset()
        val hash20 = id2.goodHashCode(murmur3F)  // progressive update hash engine
        murmur3F.reset()
        val hash11 = id1.goodHashCode(murmur3F)
        murmur3F.reset()
        val hash21 = id2.goodHashCode(murmur3F)
        Assert.assertEquals(hash10, hash11)
        Assert.assertEquals(hash20, hash21)
        Assert.assertNotEquals(hash10, hash20)
        Assert.assertNotEquals(hash10, hash21)
        Assert.assertNotEquals(hash11, hash20)
        Assert.assertNotEquals(hash11, hash21)
    }
}
