package com.ringoid.utility.test;

import com.ringoid.utility.SysTimber;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.PublishSubject;

public class RxJavaTest {

    @Test
    public void testBufferDebounce() {
        PublishSubject<Integer> ps = PublishSubject.create();

        TestScheduler sch = new TestScheduler();

        ps.compose(com.ringoid.utility.RxUtilsKt.bufferDebounce(200, TimeUnit.MILLISECONDS, sch))
                .subscribe(v ->
                    SysTimber.v(sch.now(TimeUnit.MILLISECONDS) + ": " + v),
                    Throwable::printStackTrace,
                    () -> SysTimber.v("Done"));

        ps.onNext(1);
        ps.onNext(2);
        sch.advanceTimeTo(100, TimeUnit.MILLISECONDS);
        ps.onNext(3);
        sch.advanceTimeTo(150, TimeUnit.MILLISECONDS);
        ps.onNext(4);
        sch.advanceTimeTo(400, TimeUnit.MILLISECONDS);
        ps.onNext(5);
        sch.advanceTimeTo(450, TimeUnit.MILLISECONDS);
        ps.onNext(6);
        sch.advanceTimeTo(800, TimeUnit.MILLISECONDS);
        ps.onNext(7);
        ps.onComplete();
        sch.advanceTimeTo(850, TimeUnit.MILLISECONDS);
    }
}
