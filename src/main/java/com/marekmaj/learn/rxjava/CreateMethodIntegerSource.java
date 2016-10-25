package com.marekmaj.learn.rxjava;


import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateMethodIntegerSource implements IntegerSource {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final long frequency;

    public CreateMethodIntegerSource(long frequency) {
        this.frequency = frequency;
    }

    public CreateMethodIntegerSource() {
        this(1000);
    }

    @Override
    public Observable<Integer> observe(boolean startWithError) {
        AtomicBoolean shouldThrowException = new AtomicBoolean(startWithError);
        AtomicInteger counter = new AtomicInteger(0);

        // TODO should I use emitter? create should call onCompleted
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                schedule(subscriber);
            }

            private void schedule(Subscriber<? super Integer> subscriber) {
                Runnable runnable = () -> {
                    if (subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                        return;
                    }

                    if (shouldThrowException.getAndSet(false)) {
                        subscriber.onError(new InjectedException());
                        return;
                    }

                    try {
                        subscriber.onNext(counter.incrementAndGet());
                        schedule(subscriber);
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                };

                executor.schedule(runnable, frequency, TimeUnit.MILLISECONDS);
            }

        });
    }
}
