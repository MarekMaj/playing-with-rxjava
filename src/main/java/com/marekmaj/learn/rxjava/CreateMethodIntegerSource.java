package com.marekmaj.learn.rxjava;


import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.atomic.AtomicBoolean;

public class CreateMethodIntegerSource implements IntegerSource {
    private final IntegerFeed feed;

    public CreateMethodIntegerSource(IntegerFeed integerFeed) {
        this.feed = integerFeed;
    }

    @Override
    public Observable<Integer> observe(boolean startWithError) {
        AtomicBoolean shouldThrowException = new AtomicBoolean(startWithError);

        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                Listener listener = i -> {
                    if (subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                        return;
                    }

                    if (shouldThrowException.getAndSet(false)) {
                        subscriber.onError(new InjectedException());
                        return;
                    }

                    try {
                        subscriber.onNext(i);
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                };
                feed.subscribe(listener);
                feed.init();
            }
        });
    }
}
