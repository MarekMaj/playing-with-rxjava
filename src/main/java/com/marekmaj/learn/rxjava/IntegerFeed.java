package com.marekmaj.learn.rxjava;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class IntegerFeed {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<Listener> listeners = Lists.newArrayList();
    private final AtomicInteger counter = new AtomicInteger();
    private final long frequency;

    public IntegerFeed(long frequency) {
        this.frequency = frequency;
    }

    public void subscribe(Listener listener) {
        this.listeners.add(listener);
    }

    public void init() {
        scheduler.scheduleAtFixedRate(() -> {
            listeners.forEach(l -> l.event(counter.incrementAndGet()));
        }, 0, frequency, TimeUnit.MILLISECONDS);
    }
}
