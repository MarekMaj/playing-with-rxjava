package com.marekmaj.learn.rxjava;

import org.junit.Test;
import rx.Observable;
import rx.exceptions.MissingBackpressureException;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


public class BackpressureTest {


    @Test
    public void shouldThrowExceptionWhenNaiveProducerIsFasterAndNoBackpressureAtAll() {
        AtomicReference<Throwable> caughtException = new AtomicReference<>();
        Observable<Integer> fastNaiveObservable = new CreateMethodIntegerSource(1).observe()
                .observeOn(Schedulers.io());

        fastNaiveObservable.subscribe(
                integer -> sleep(1000),
                caughtException::set);

        await().atMost(5, TimeUnit.SECONDS).until(() -> caughtException.get() != null);
        assertThat(caughtException.get()).isInstanceOf(MissingBackpressureException.class);
    }


    private void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (Exception e) { }
    }
}
