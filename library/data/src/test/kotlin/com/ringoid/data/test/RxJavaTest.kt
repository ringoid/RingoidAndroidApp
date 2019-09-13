package com.ringoid.data.test

import com.ringoid.data.executor.UseCaseThreadExecutorImpl
import com.ringoid.utility.SysTimber
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import java.util.concurrent.TimeUnit

class RxJavaTest {

    data class Model(val name: String)

    @Test
    fun delayEmissionThread_someObservableWithDelay_seeHowThreadsAreSwitched() {
        val logs = mutableListOf<String>()
        val executor = UseCaseThreadExecutorImpl()

        Single.just(Model("Maxim"))
            .subscribeOn(Schedulers.from(executor))
            .doOnSubscribe { logs.add("On outer subscribe: ${threadInfo()}") }
            .flatMap {
                Single.just(it.name)
                      .doOnSubscribe { logs.add("On inner subscribe: ${threadInfo()}") }
            }
            .delay(100L, TimeUnit.MILLISECONDS)
            .map {
                logs.add("On map: ${threadInfo()}")
                it.length
            }
            .subscribeOn(Schedulers.io())
            .subscribe({ logs.add("On success: ${threadInfo()}") }, SysTimber::e)

        Thread.sleep(500L)
        logs.forEach { SysTimber.v(it) }
    }

    private fun threadInfo(): String = "[tid=${Thread.currentThread().id}, name=${Thread.currentThread().name}]"
}
