package com.ringoid.data.test

import com.ringoid.data.executor.UseCaseThreadExecutorImpl
import com.ringoid.report.exception.SimulatedException
import com.ringoid.utility.SysTimber
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

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

    @Test
    fun concatMap_someObservableWithSomeFailingEmissions_seeHowErrorsDelivered() {
        val logs = mutableListOf<String>()
        var index = 0

        Observable.range(1, 10)
            .delay(100L, TimeUnit.MILLISECONDS)
            .concatMapSingle {
                val source = if (it % 3 == 0) Single.error(SimulatedException("i=$it"))
                else Single.just(it * it)

                source.onErrorResumeNext { e ->
                    logs.add("Error resume [${index++}]: $e")
                    Single.error(e)
                }
            }
            .doOnError { logs.add("ERROR: $it") }
            .doOnNext { logs.add("On next [${index++}]: ${sqrt(it.toFloat()).toInt()}") }
            .toList()
            .subscribe({ logs.add("On success: ${it.joinToString()}") },
                {
                    logs.add("On error: $it")
                    (it as? CompositeException)?.let { ce ->
                        logs.add("\t\t composite error of size ${ce.size()}")
                        ce.exceptions.forEach { e -> logs.add("\t\t error: $e") }
                    }
                })

        Thread.sleep(1200L)
        logs.forEach { SysTimber.v(it) }
    }

    @Test
    fun concatMapIndexed_someObservableWithSomeFailingEmissions_seeHowErrorsDelivered() {
        val ACTIONS_CHUNK_SIZE = 60
        val logs = mutableListOf<String>()

        Observable.range(1, 10)
            .delay(100L, TimeUnit.MILLISECONDS)
            .zipWith(Observable.range(1, 10), BiFunction { v: Int, i: Int -> v to i })
            .concatMapSingle { (it, index) ->
                val source = if (it % 4 == 0) Single.error(SimulatedException("i=$it"))
                else Single.just((it * ACTIONS_CHUNK_SIZE) to index)

                source.onErrorResumeNext { e ->
                    logs.add("Error resume [$index]: $e")
                    Single.error(e)
                }
            }
            .doOnError { logs.add("ERROR: $it") }
            .doOnNext { (it, index) -> logs.add("On next [$index]: $it") }
            .toList()
            .subscribe({ logs.add("On success: ${it.joinToString()}") },
                {
                    logs.add("On error: $it")
                    (it as? CompositeException)?.let { ce ->
                        logs.add("\t\t composite error of size ${ce.size()}")
                        ce.exceptions.forEach { e -> logs.add("\t\t error: $e") }
                    }
                })

        Thread.sleep(1200L)
        logs.forEach { SysTimber.v(it) }
    }

    @Test
    fun flatMapDelayErrors_someObservableWithSomeFailingEmissions_seeHowErrorsDelivered() {
        val logs = mutableListOf<String>()
        var index = 0

        Observable.range(1, 10)
            .delay(100L, TimeUnit.MILLISECONDS)
            .flatMapSingle({
                val source = if (it % 3 == 0) Single.error(SimulatedException("i=$it"))
                             else Single.just(it * it)

                source.onErrorResumeNext { e ->
                    logs.add("Error resume [${index++}]: $e")
                    Single.error(e)
                }
            }, true)
            .doOnError { logs.add("ERROR: $it") }
            .doOnNext { logs.add("On next [${index++}]: ${sqrt(it.toFloat()).toInt()}") }
            .toList()
            .subscribe({ logs.add("On success: ${it.joinToString()}") },
                       {
                           logs.add("On error: $it")
                           (it as? CompositeException)?.let { ce ->
                               logs.add("\t\t composite error of size ${ce.size()}")
                               ce.exceptions.forEach { e -> logs.add("\t\t error: $e") }
                           }
                       })

        Thread.sleep(1200L)
        logs.forEach { SysTimber.v(it) }
    }

    private fun threadInfo(): String = "[tid=${Thread.currentThread().id}, name=${Thread.currentThread().name}]"
}
