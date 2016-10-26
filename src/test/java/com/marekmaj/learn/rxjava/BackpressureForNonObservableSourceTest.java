package com.marekmaj.learn.rxjava;

import com.google.common.collect.Lists;
import org.junit.Test;
import rx.AsyncEmitter;
import rx.Observable;
import rx.exceptions.MissingBackpressureException;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


public class BackpressureForNonObservableSourceTest {


    @Test
    public void shouldThrowExceptionWhenNaiveProducerIsFasterAndNoBackpressureAtAll() {
        AtomicReference<Throwable> caughtException = new AtomicReference<>();
        AtomicReference<List<Integer>> elements = new AtomicReference<>(Lists.newArrayList());

        Observable<Integer> fastNaiveObservable = new CreateMethodIntegerSource(new IntegerFeed(1)).observe()
                .observeOn(Schedulers.io());

        fastNaiveObservable.subscribe(
                integer -> { sleep(1000); elements.get().add(integer);},
                caughtException::set);

        await().atMost(5, TimeUnit.SECONDS).until(() -> caughtException.get() != null);

        assertThat(caughtException.get()).isInstanceOf(MissingBackpressureException.class);
        assertThat(elements.get()).containsExactly(1);
    }

    @Test
    public void shouldThrowExceptionFromEmitterWhenNoBackpressureMode() {
        AtomicReference<Throwable> caughtException = new AtomicReference<>();
        AtomicReference<List<Integer>> elements = new AtomicReference<>(Lists.newArrayList());

        Observable<Integer> emitterObservable = new EmitterIntegerSource(new IntegerFeed(1), AsyncEmitter.BackpressureMode.NONE).observe()
                .observeOn(Schedulers.io());

        emitterObservable.subscribe(
                integer -> { sleep(1000); elements.get().add(integer);},
                caughtException::set);

        await().atMost(5, TimeUnit.SECONDS).until(() -> caughtException.get() != null);

        assertThat(caughtException.get()).isInstanceOf(MissingBackpressureException.class);
        assertThat(elements.get()).containsExactly(1);
    }

    @Test
    public void shouldUseBackpressureUnboundedQueueWithEmitter() {
        AtomicReference<Throwable> caughtException = new AtomicReference<>();
        AtomicReference<List<Integer>> elements = new AtomicReference<>(Lists.newArrayList());

        // buffer is unbounded queue which can cause memory error
        Observable<Integer> emitterObservable = new EmitterIntegerSource(new IntegerFeed(1), AsyncEmitter.BackpressureMode.BUFFER).observe()
                .observeOn(Schedulers.io());

        emitterObservable.subscribe(
                integer -> { sleep(1000); elements.get().add(integer);},
                caughtException::set);

        await().atLeast(5, TimeUnit.SECONDS).until(() -> elements.get().size() >= 5);

        assertThat(caughtException.get()).isNull();
        assertThat(elements.get()).containsSequence(1,2,3,4,5);
    }

    @Test
    public void shouldUseClientBackpressureBuffer() {
        AtomicReference<Throwable> caughtException = new AtomicReference<>();
        AtomicReference<List<Integer>> elements = new AtomicReference<>(Lists.newArrayList());

        Observable<Integer> emitterObservable = new EmitterIntegerSource(new IntegerFeed(1), AsyncEmitter.BackpressureMode.NONE).observe()
                .onBackpressureBuffer()
                .observeOn(Schedulers.io());

        emitterObservable.subscribe(
                integer -> { sleep(1000); elements.get().add(integer);},
                caughtException::set);

        await().atLeast(5, TimeUnit.SECONDS).until(() -> elements.get().size() >= 5);

        assertThat(caughtException.get()).isNull();
        assertThat(elements.get()).containsSequence(1,2,3,4,5);
    }

    // for non-observable source where we cannot use reactive pull - request(n) - and as buffer may cause OutOfMemory this may be a way to go
    @Test
    public void shouldProcessSafelyWithThrottling() {
        AtomicReference<Throwable> caughtException = new AtomicReference<>();
        AtomicReference<List<Integer>> elements = new AtomicReference<>(Lists.newArrayList());

        Observable<Integer> emitterObservable = new EmitterIntegerSource(new IntegerFeed(1), AsyncEmitter.BackpressureMode.NONE).observe()
                .throttleFirst(100, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io());

        emitterObservable.subscribe(
                integer -> { sleep(1000); elements.get().add(integer);},
                caughtException::set);

        await().atLeast(5, TimeUnit.SECONDS).until(() -> elements.get().size() >= 5);

        assertThat(caughtException.get()).isNull();
        assertThat(elements.get()).contains(1).doesNotContain(2,3,4,5);
    }

    private void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (Exception e) { }
    }
}
