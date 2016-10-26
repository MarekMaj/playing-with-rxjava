package com.marekmaj.learn.rxjava;

import com.google.common.collect.Lists;
import rx.AsyncEmitter;
import rx.Observable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class EmitterIntegerSource implements IntegerSource {

    private final IntegerFeed feed = new IntegerFeed();
    private final AsyncEmitter.BackpressureMode mode;

    public EmitterIntegerSource(AsyncEmitter.BackpressureMode mode) {
        this.mode = mode;
    }

    @Override
    public Observable<Integer> observe(boolean startWithError) {
        return Observable.fromEmitter(integerAsyncEmitter -> {
            Listener listener = integerAsyncEmitter::onNext;
            feed.subscribe(listener);
            feed.init();
        }, mode);
    }

    private final class IntegerFeed {
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private final List<Listener> listeners = Lists.newArrayList();
        private final AtomicInteger counter = new AtomicInteger();

        public void subscribe(Listener listener) {
            this.listeners.add(listener);
        }

        public void init() {
            scheduler.scheduleAtFixedRate(() -> {
                listeners.forEach(l -> l.event(counter.incrementAndGet()));
            }, 0, 1, TimeUnit.MILLISECONDS);
        }
    }

    // TODO onError and complete with shutdown methods
    private interface Listener {
        void event(int i);
    }
}
