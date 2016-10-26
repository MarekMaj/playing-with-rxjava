package com.marekmaj.learn.rxjava;


import com.google.common.collect.Lists;
import org.junit.Test;
import rx.AsyncEmitter;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class WindowAndBufferTest {

    @Test
    public void shouldBufferFastObservable() {
        AtomicReference<List<Integer>> elements = new AtomicReference<>(Lists.newArrayList());

        new EmitterIntegerSource(new IntegerFeed(1), AsyncEmitter.BackpressureMode.NONE).observe()
                .buffer(1000)
                .observeOn(Schedulers.io())
                .subscribe(
                        list -> { elements.get().add(list.get(0));});

        await().atLeast(5, TimeUnit.SECONDS).until(() -> elements.get().size() >= 5);

        assertThat(elements.get()).containsSequence(1,1001,2001,3001,4001);
    }

    @Test
    public void shouldWindowFastObservable() {
        AtomicReference<List<Integer>> elements = new AtomicReference<>(Lists.newArrayList());

        new EmitterIntegerSource(new IntegerFeed(1), AsyncEmitter.BackpressureMode.NONE).observe()
                .window(1000)
                .flatMap(Observable::first)
                .observeOn(Schedulers.io())
                .subscribe(
                        integer -> { elements.get().add(integer);});

        await().atLeast(4, TimeUnit.SECONDS).until(() -> elements.get().size() >= 5);

        assertThat(elements.get()).containsSequence(1,1001,2001,3001,4001);
    }
}
