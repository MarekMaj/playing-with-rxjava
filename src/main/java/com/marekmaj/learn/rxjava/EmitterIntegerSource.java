package com.marekmaj.learn.rxjava;

import rx.AsyncEmitter;
import rx.Observable;

import java.util.concurrent.atomic.AtomicBoolean;


public class EmitterIntegerSource implements IntegerSource {

    private final IntegerFeed feed;
    private final AsyncEmitter.BackpressureMode mode;

    public EmitterIntegerSource(IntegerFeed feed, AsyncEmitter.BackpressureMode mode) {
        this.feed = feed;
        this.mode = mode;
    }

    @Override
    public Observable<Integer> observe(boolean startWithError) {
        AtomicBoolean shouldThrowException = new AtomicBoolean(startWithError);
        return Observable.fromEmitter(integerAsyncEmitter -> {
            Listener listener = i -> {
                if (shouldThrowException.getAndSet(false)) {
                    integerAsyncEmitter.onError(new InjectedException());
                    return;
                }
                integerAsyncEmitter.onNext(i);
            };
            feed.subscribe(listener);
            feed.init();
        }, mode);
    }

}
