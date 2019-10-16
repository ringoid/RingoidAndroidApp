package com.ringoid.utility.test

import com.ringoid.utility.SysTimber
import com.ringoid.utility.bufferDebounce
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import java.util.concurrent.TimeUnit

class RxKotlinTest {

    @Test
    fun testBufferDebounce() {
        val source = PublishSubject.create<String>()
        val testScheduler = TestScheduler()

        source.compose(bufferDebounce(200, TimeUnit.MILLISECONDS, testScheduler))
            .subscribe({
                SysTimber.v("${testScheduler.now(TimeUnit.MILLISECONDS)}: $it")
            }, SysTimber::e)

        source.onNext("A")
        source.onNext("B")
        testScheduler.advanceTimeTo(100, TimeUnit.MILLISECONDS)
        source.onNext("C")
        testScheduler.advanceTimeTo(150, TimeUnit.MILLISECONDS)
        source.onNext("D")
        testScheduler.advanceTimeTo(400, TimeUnit.MILLISECONDS)
        source.onNext("E")
        testScheduler.advanceTimeTo(450, TimeUnit.MILLISECONDS)
        source.onNext("F")
        testScheduler.advanceTimeTo(800, TimeUnit.MILLISECONDS)
        source.onNext("G")
        source.onComplete()
    }
}
